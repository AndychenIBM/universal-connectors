# Universal Connector
This project is meant to be internal, and contain a container with _logstash_ installed with output plugin that will send the output to Guardium machines.

Configuration will be communicated by another project, that will contain:
1. Datasource configration – Host, port, tokens, ...
2. Data transformation definitions – transform the log into a standard Guardium JSON, and 
3. A list of Guardium machines to send this data to and a strategy (either replication, failover, or load-balancing)

Good luck to us all!
