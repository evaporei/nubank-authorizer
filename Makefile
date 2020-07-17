docker-build:
	@docker build -t nubank-authorizer .

docker-run:
	@docker run -i nubank-authorizer <&0


.PHONY: docker-build docker-run
