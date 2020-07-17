FROM clojure:alpine
MAINTAINER Otávio Pace <otaviopp8@gmail.com>

WORKDIR /nubank-authorizer

COPY project.clj project.clj
RUN lein deps

COPY src src

CMD ["lein", "run"]
