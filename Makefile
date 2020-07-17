build-app:
	@docker build --target app -t nubank-authorizer-app .

run:
	@docker run -i nubank-authorizer-app <&0

build-test:
	@docker build --target test -t nubank-authorizer-test .

test:
	@docker run nubank-authorizer-test

lint:
	@docker run -v $(shell pwd)/src:/src -v $(shell pwd)/test:/test --rm borkdude/clj-kondo clj-kondo --lint src test


.PHONY: build-app run build-test test lint
