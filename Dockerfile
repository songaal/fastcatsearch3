FROM openjdk:8-jre

ENV FASTCATSEARCH_HOME /usr/local/fastcatsearch3
ENV PATH $FASTCATSEARCH_HOME/bin:$PATH
RUN mkdir -p "$FASTCATSEARCH_HOME"
WORKDIR $FASTCATSEARCH_HOME

RUN set -x \
	&& apt-get update

ARG GNCLOUD_REPOSITORY
ENV FASTCATSEARCH_VERSION 3.14.2
ENV FASTCATSEARCH_TGZ_URL $GNCLOUD_REPOSITORY/fastcatsearch/fastcatsearch-$FASTCATSEARCH_VERSION.tar.gz

RUN set -x \
	\
	&& wget -O fastcatsearch3.tar.gz "$FASTCATSEARCH_TGZ_URL" \
	&& tar -xvf fastcatsearch3.tar.gz --strip-components=1

VOLUME $FASTCATSEARCH_HOME/collections

EXPOSE 8090
ENTRYPOINT ["/bin/fastcatsearch3"]
CMD ["run"]