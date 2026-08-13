compile:
	mvn -DskipTests compile

unit-test:
	mvn test -Dtest='!za.co.wethinkcode.robots.acceptencetest.**'

acceptance-test:
	./scripts/run-all-acceptance-tests.sh

package:
	mvn -DskipTests package

ci: compile unit-test package acceptance-test
