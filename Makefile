build-app:
	@docker build --target app -t nubank-authorizer-app .

run:
	@docker run -i nubank-authorizer-app <&0

build-test:
	@docker build --target test -t nubank-authorizer-test .

test:
	@docker run nubank-authorizer-test


.PHONY: build-app run build-test test
