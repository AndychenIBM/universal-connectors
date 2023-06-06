package com.ibm.guardium.universalconnector.transformer;

import com.ibm.guardium.proto.datasource.Datasource;
import com.ibm.guardium.universalconnector.commons.structures.Record;

import java.util.List;

public interface RecordTransformer {

    List<Datasource.Guard_ds_message> transform(Record record);
}
