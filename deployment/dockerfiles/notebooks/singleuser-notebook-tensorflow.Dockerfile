FROM jupyter/pyspark-notebook:latest

USER root

RUN PYCURL_SSL_LIBRARY=openssl pip3 install tensorflow

USER ${NB_UID}
WORKDIR "${HOME}"
