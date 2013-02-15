package it.phoops.geoserver.ols.solr.utils;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;

public class SolrPager {
    public static SolrDocumentList query(SolrServer server, ModifiableSolrParams params) throws SolrServerException {
        SolrDocumentList        retval = new SolrDocumentList();
        int                     start = 0;
        QueryResponse           qr;
        SolrDocumentList        list;
        
        do {
            params.set("start", start);
            
            qr = server.query(params);
            list = qr.getResults();
            
            retval.addAll(list);
            start = retval.size();
        } while (start < list.getNumFound());
        
        return retval;
    }
}
