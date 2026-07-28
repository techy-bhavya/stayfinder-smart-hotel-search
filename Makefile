.PHONY: up down logs test clean

up:
	docker compose up --build

down:
	docker compose down

logs:
	docker compose logs -f

test:
	cd backend && mvn test
	cd frontend && npm install && npm run build

clean:
	docker compose down -v --remove-orphans
