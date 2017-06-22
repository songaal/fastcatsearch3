FROM openjdk:8-jre

ENV FASTCATSEARCH_HOME /usr/local/fastcatsearch3
ENV PATH $FASTCATSEARCH_HOME/bin:$PATH

RUN set -x \
	&& apt-get update

RUN mkdir -p "$FASTCATSEARCH_HOME"

# ARG GNCLOUD_REPOSITORY
ARG LIB
ARG FASTCATSEARCH_VERSION=3.14.3
ARG KOREAN_VERSION=2.21.5
ARG PRODUCT_VERSION=2.23.2

WORKDIR "$FASTCATSEARCH_HOME"

COPY $LIB/fastcatsearch-$FASTCATSEARCH_VERSION.tar.gz "$FASTCATSEARCH_HOME"

RUN set -x \
	\
	&& tar -xzvf fastcatsearch-$FASTCATSEARCH_VERSION.tar.gz --strip-components=1 \
	&& ls && echo `pwd`

WORKDIR $FASTCATSEARCH_HOME/plugin/analysis/

COPY $LIB/analyzer-korean-$KOREAN_VERSION.tar.gz $LIB/analyzer-product-$PRODUCT_VERSION.tar.gz ./

RUN set -x \
	\
	&& tar -xzvf analyzer-korean-$KOREAN_VERSION.tar.gz --strip-components=1 \
	&& tar -xzvf analyzer-product-$PRODUCT_VERSION.tar.gz --strip-components=1

WORKDIR $FASTCATSEARCH_HOME

RUN sed 's/<!-- appender-ref ref="STDOUT" \/-->/<appender-ref ref="STDOUT" \/>/' conf/logback.xml > logback.xml
RUN mv ./logback.xml conf/logback.xml
RUN sed 's/root level="debug"/root level="info"/' conf/logback.xml > logback.xml
RUN mv ./logback.xml conf/logback.xml

#VOLUME $FASTCATSEARCH_HOME/collections

EXPOSE 8090
ENTRYPOINT ["/bin/bash", "bin/fastcatsearch"]
CMD ["run"]