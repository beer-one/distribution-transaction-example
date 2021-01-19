docker run -d -p 6000:6000 --name account-service account-service:1.0;
docker run -d -p 6010:6010 --name order-service order-service:1.0;
docker run -d -p 6020:6020 --name product-service product-service:1.0;
docker run -d -p 6030:6030 --name transaction-service transaction-service:1.0;

