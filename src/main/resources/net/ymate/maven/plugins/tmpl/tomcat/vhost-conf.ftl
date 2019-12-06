# Apache2 VHost Config
<VirtualHost *:80>
    ServerAdmin postmaster@<#if host_name?starts_with("www.")>${host_name?substring(4)}<#else>${host_name}</#if>
    ServerName ${host_name}
<#if host_alias != ''>ServerAlias ${host_alias}</#if>
    DocumentRoot ${website_root_path}
    DirectoryIndex index.html index.jsp

    JkMount /* ${service_name}

    <Directory ${website_root_path}>
        Options -Indexes FollowSymLinks
        AllowOverride None
        Order allow,deny
        Allow from all
    </Directory>
</VirtualHost>

# JK AJP1.3 Config
worker.list=${service_name}
worker.${service_name}.port=${ajp_port}
worker.${service_name}.host=${ajp_host}
worker.${service_name}.type=ajp13
worker.${service_name}.lbfactor=1

# Nginx Config
upstream ${service_name} {
    ip_hash;
    server  127.0.0.1:${connector_port};
}

server {
    listen      80 default;
    server_name _;
    return      500;
}

server {
    listen 80;
    server_name ${host_name} ${host_alias};

    if ($request_method !~ ^(GET|POST|HEAD)$ ) {
        return 405;
    }

    location ~ ^/ShowMeTheStatus/ {
        stub_status on;
        access_log  off;
    }

    location ~ /.svn/ {
        return 404;
    }

    location ~ /.git/ {
        return 404;
    }

    location ~ ^/(WEB-INF)/ {
        return 404;
    }

    location ~ \.(apk|torrent|htm|html|asp|php|gif|jpg|jpeg|png|bmp|ico|rar|css|js|zip|map|java|jar|txt|flv|swf|mid|doc|ppt|xls|pdf|txt|mp3|wma)$ {
        root        ${website_root_path};
        access_log  off;
        expires     30d;
    }

    location / {
        proxy_pass                          http://${service_name};
        access_log                          off;
        proxy_redirect                      off;
        proxy_set_header Host               $host;
        proxy_set_header X-Real-IP          $remote_addr;
        proxy_set_header X-Forwarded-For    $proxy_add_x_forwarded_for;
        proxy_set_header                    X-Forwarded-Proto $scheme;
        proxy_set_header                    Accept-Encoding 'gzip';
        client_max_body_size                10m;
        client_body_buffer_size             256k;
        proxy_connect_timeout               500;
        proxy_send_timeout                  2000;
        proxy_read_timeout                  2000;
        proxy_ignore_client_abort           on;

        proxy_http_version                  1.1;

        proxy_buffer_size                   128k;
        proxy_buffers                       4 256k;
        proxy_busy_buffers_size             256k;
        proxy_temp_file_write_size          256k;
    }
}