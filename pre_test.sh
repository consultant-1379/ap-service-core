echo "Running pre_test.sh"

docker images

docker rmi armdocker.rnd.ericsson.se/proj_oss/autoprovisioning/jboss_service

echo "jboss_service image removed"