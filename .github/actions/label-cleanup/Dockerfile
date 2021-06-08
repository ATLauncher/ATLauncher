FROM andrius/alpine-ruby

RUN apk update && \
    apk upgrade && \
    apk add bash curl-dev ruby-dev build-base git ruby ruby-io-console ruby-bundler ruby-json ruby-rdoc && \
    rm -rf /var/cache/apk/*

COPY entrypoint.sh /entrypoint.sh
COPY action.rb /action.rb

RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
