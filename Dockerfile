FROM opencadc/tomcat:alpine

COPY LocalAuthority.properties /root/config/
COPY build/libs/*.war webapps/

EXPOSE 5555

