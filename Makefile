build-app:
	@docker build --target app -t nubank-authorizer-app .

run:
	@docker run -i nubank-authorizer-app <&0

build-test:
	@docker build --target test -t nubank-authorizer-test .

test:
	@docker run nubank-authorizer-test

lint:
	@docker run -v $(shell pwd)/src:/src --rm borkdude/clj-kondo clj-kondo --lint src


.PHONY: build-app run build-test test lint
