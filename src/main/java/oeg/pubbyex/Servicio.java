package oeg.pubbyex;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import de.fuberlin.wiwiss.pubby.Configuration;
import de.fuberlin.wiwiss.pubby.servlets.BaseServlet;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

/**
 * Cómo construir este proyecto: mvn clean install -DskiptTests
 * -Dmaven.javadoc.skip=true http://patents.linkeddata.es/data/api/test
 *
 * @author Victor
 */
public class Servicio extends BaseServlet {

    static final String endpoint = "http://patents.linkeddata.es/sparql";

    
    public String getSPARQL(HttpServletRequest request)
    {
            String p_keywords = request.getParameter("p_keywords");
            String i_name = request.getParameter("i_name");
            String a_name = request.getParameter("a_name");
            String p_office = request.getParameter("p_office");
            String p_country = request.getParameter("p_office");
            
            String prefijos = "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX dc: <http://purl.org/dc/elements/1.1/> PREFIX : <http://dbpedia.org/resource/> PREFIX dbpedia2: <http://dbpedia.org/property/> PREFIX dbpedia: <http://dbpedia.org/> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> ";
            String sparql = "_TEMPLATE1_";
            sparql+=" WHERE {\n"
                    + "GRAPH <http://patents.linkeddata.es/> {";
            
            sparql += "?pat a <http://w3id.org/pmo.owl#PatentForInvention> .\n";
            sparql += "?pat <http://purl.org/dc/terms/title> ?title .\n";
            
            if (p_office!=null &&!p_office.isEmpty()) {
                sparql += "?pat <http://purl.org/dc/terms/publisher> \"" + p_office + "\" .";
            }
            if (i_name!=null && !i_name.isEmpty()) {
                sparql += "?pat <http://w3id.org/pmo.owl#inventor> ?i .\n";
                sparql += "?i <http://purl.org/dc/terms/title> ?iname .\n";
            }
            if (a_name!=null && !a_name.isEmpty()) {
                sparql += "?pat <http://w3id.org/pmo.owl#applicant> ?a .\n";
                sparql += "?a <http://purl.org/dc/terms/title> ?aname .\n";
            }
            if (p_country!=null && !p_country.isEmpty()) {
                sparql += "?pat <http://w3id.org/pmo.owl#countryOfFiling> \"" + p_country + "\" .";
            }
            
            sparql+=" }"; 
            
            if (p_keywords!=null && !p_keywords.isEmpty())
            {
                sparql+="FILTER regex(?title, \"(?:^|(?<= ))("+p_keywords +")(?:(?= )|$)\", \"i\") .";
            }
            if (i_name!=null && !i_name.isEmpty()) {
                sparql+="FILTER regex(?iname, \"(?:^|(?<= ))("+i_name +")(?:(?= )|$)\", \"i\") .";
            }
            if (a_name!=null && !a_name.isEmpty()) {
                sparql+="FILTER regex(?aname, \"(?:^|(?<= ))("+a_name +")(?:(?= )|$)\", \"i\") .";
            }
            sparql+= "} LIMIT 10\n";        
            return prefijos+sparql;
    }
    
    
    public boolean doGet(String relativeURI,
            HttpServletRequest request,
            HttpServletResponse response,
            Configuration config) throws ServletException, IOException {

        String value = request.getParameter("uri");

        if (value == null) {
            value = "";
        }

        if (request.getPathInfo().contains("/search")) {
            response.setContentType("text/plain;charset=UTF-8");
            String answer = "";
            String sparql = getSPARQL(request);
            sparql = sparql.replace("_TEMPLATE1_", " SELECT * ");
            Query query = QueryFactory.create(sparql);
            QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
            try {
                ResultSet results = qexec.execSelect();
                for (; results.hasNext();) {
                    QuerySolution qs = results.next();
                    String pat = qs.get("?pat").asResource().getURI();
                    String title = qs.get("?title").asLiteral().getLexicalForm();
                    answer += "<a href=\""+pat+"\">"+ title + "</a><br>\n";
                }
            } catch (Exception e) {
                PrintWriter out = response.getWriter();
                String s = e.getMessage();
                out.println(s);
                return true;                
            }
            PrintWriter out = response.getWriter();
            out.println(answer);
            return true;
        } else if (request.getPathInfo().contains("/count")) {
            response.setContentType("text/plain;charset=UTF-8");
            String answer = "no answer";
            String sparql = getSPARQL(request);
            sparql = sparql.replace("_TEMPLATE1_", " select (count(*) as ?count) ");
            Query query = QueryFactory.create(sparql);
            QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
            try {
                ResultSet results = qexec.execSelect();
                for (; results.hasNext();) {
                    QuerySolution qs = results.next();
                    String count = qs.get("?count").asLiteral().getLexicalForm();
                    answer = "Hay un total de " + count + " patentes.";
                }
            } catch (Exception e) {
                PrintWriter out = response.getWriter();
                String s = e.getMessage();
                out.println(s);
                return true;                
            }
            PrintWriter out = response.getWriter();
            out.println(answer);
            return true;
        } else {
            response.setContentType("text/plain;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("pong " + relativeURI + " " + value + " " + request.getPathInfo());
        }

        return true;
    }

}
