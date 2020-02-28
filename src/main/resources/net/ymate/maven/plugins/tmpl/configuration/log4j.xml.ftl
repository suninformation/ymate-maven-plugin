<?xml version="1.0" encoding="UTF-8"?>
<Configuration>
    <Appenders>
        <RollingRandomAccessFile name="default"
                                 fileName="${r'${sys:LOG_OUT_DIR}'}/default.log"
                                 filePattern="${r'${sys:LOG_OUT_DIR}/$${date:yyyyMMdd}/default-%d{yyMMddHH}-%i.log'}">
            <PatternLayout pattern="%m %n" charset="UTF-8"/>
            <Policies>
                <TimeBasedTriggeringPolicy modulate="true" interval="1"/>
                <SizeBasedTriggeringPolicy size="200 MB"/>
            </Policies>
            <DefaultRolloverStrategy max="100"/>
        </RollingRandomAccessFile>
        <!--
        <RollingRandomAccessFile name="custom-logname"
                                 fileName="${r'${sys:LOG_OUT_DIR}'}/custom-logname.log"
                                 filePattern="${r'${sys:LOG_OUT_DIR}/$${date:yyyyMMdd}/custom-logname-%d{yyMMddHH}-%i.log'}">
            <PatternLayout pattern="%m %n" charset="UTF-8"/>
            <Policies>
                <TimeBasedTriggeringPolicy modulate="true" interval="1"/>
                <SizeBasedTriggeringPolicy size="200 MB"/>
            </Policies>
            <DefaultRolloverStrategy max="100"/>
        </RollingRandomAccessFile>
        -->
    </Appenders>
    <Loggers>
        <!--
        <Logger name="custom-logname" level="debug" additivity="false">
            <AppenderRef ref="custom-logname"/>
        </Logger>
        -->
        <Root level="debug">
            <AppenderRef ref="default"/>
        </Root>
    </Loggers>
</Configuration>