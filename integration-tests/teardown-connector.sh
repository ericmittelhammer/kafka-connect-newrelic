while getopts t: flag
do
    case "${flag}" in
        t) topic=${OPTARG};;
    esac
done
echo "Deleting connector: $topic";

# delete the connector
curl --silent -X DELETE http://localhost:8083/connectors/${topic} | jq

# show the remaining connectors
echo "remaining connectors:"
curl --silent -X GET http://localhost:8083/connectors/ | jq

echo "Done"