FROM clojure:alpine AS base
WORKDIR /nubank-authorizer
COPY project.clj project.clj
RUN lein deps
COPY src src

FROM base AS app
CMD ["lein", "run"]

FROM base AS test
COPY test test
CMD ["lein", "test"]
