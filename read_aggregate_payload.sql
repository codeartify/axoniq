SELECT aggregate_identifier,
       convert_from(lo_get(payload), 'UTF8')   AS payload_json,
       convert_from(lo_get(meta_data), 'UTF8') AS metadata_json
FROM domain_event_entry
ORDER BY aggregate_identifier;
