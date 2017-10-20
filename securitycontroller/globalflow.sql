create table flow_keys (
    src_mac varchar(20),
    dst_mac varchar(20),
    src_ip varchar(20),
    dst_ip varchar(20),
    src_port integer,
    dst_port integer,
    proto integer,
    key varchar(50)
);
