FROM dna/int/pyspark-notebook-base:1.0

USER root

WORKDIR /tmp
COPY requirements-single.txt /tmp/requirements-single.txt
RUN PYCURL_SSL_LIBRARY=openssl pip3 install --no-cache-dir -r /tmp/requirements-single.txt

USER ${NB_UID}
WORKDIR "${HOME}"
