build:
	@docker build -t nubank-authorizer .

run:
	@docker run -i nubank-authorizer <&0


.PHONY: build run
