FROM openjdk:8-jre

ENV FASTCATSEARCH_HOME /usr/local/fastcatsearch3
ENV PATH $FASTCATSEARCH_HOME/bin:$PATH

RUN set -x \
	&& apt-get update

ARG LIB
ARG FASTCATSEARCH_VERSION=3.14.3
ARG KOREAN_VERSION=2.21.5
ARG PRODUCT_VERSION=2.23.2

WORKDIR /usr/local/

COPY $LIB/fastcatsearch-$FASTCATSEARCH_VERSION.tar.gz /usr/local/

RUN set -x \
	\
	&& tar -xzvf fastcatsearch-$FASTCATSEARCH_VERSION.tar.gz --strip-components=1 \
	&& mv fastcatsearch-$FASTCATSEARCH_VERSION fastcatsearch3

COPY $LIB/analyzer-korean-$KOREAN_VERSION.tar.gz $LIB/analyzer-product-$PRODUCT_VERSION.tar.gz $FASTCATSEARCH_HOME/plugin/analysis/

WORKDIR $FASTCATSEARCH_HOME/plugin/analysis/

RUN set -x \
	\
	&& tar -xzvf analyzer-korean-$KOREAN_VERSION.tar.gz --strip-components=1 \
	&& tar -xzvf analyzer-product-$PRODUCT_VERSION.tar.gz --strip-components=1 \

WORKDIR $FASTCATSEARCH_HOME

#VOLUME $FASTCATSEARCH_HOME/collections

EXPOSE 8090
ENTRYPOINT ["/bin/fastcatsearch3"]
CMD ["run"]