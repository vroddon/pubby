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
import java.util.HashSet;
import java.util.Set;
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
            String res = request.getParameter("res");

      //      String p_year = request.getParameter("p_year");
            String p_keywords = request.getParameter("p_keywords");
            String i_name = request.getParameter("i_name");
            String i_country = request.getParameter("i_country");
            i_country = i_country.toUpperCase();
            if (i_country.equals("UN"))
                i_country="";
            
            String a_name = request.getParameter("a_name");
            String a_country = request.getParameter("a_country");
            a_country = a_country.toUpperCase();
            if (a_country.equals("UN"))
                a_country="";
            
            String p_office = request.getParameter("p_office");
            String p_country = request.getParameter("p_country");
            p_country = p_country.toUpperCase();
            if (p_country.equals("UN"))
                p_country="";
            
            String prefijos = "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX dc: <http://purl.org/dc/elements/1.1/> PREFIX : <http://dbpedia.org/resource/> PREFIX dbpedia2: <http://dbpedia.org/property/> PREFIX dbpedia: <http://dbpedia.org/> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> ";
            String sparql = "_TEMPLATE1_";
            sparql+=" WHERE {\n"
                    + "GRAPH <http://patents.linkeddata.es/> {";
            
            sparql += "?pat a <http://w3id.org/pmo.owl#PatentForInvention> .\n";
            sparql += "?pat <http://purl.org/dc/terms/title> ?title .\n";
            
            //RESTRICCIONES SOBRE LA PATENTE
            if (p_office!=null &&!p_office.isEmpty()) {
                sparql += "?pat <http://purl.org/dc/terms/publisher> \"" + p_office + "\" .";
            }
            if (p_country!=null && !p_country.isEmpty()) {
                sparql += "?pat <http://w3id.org/pmo.owl#countryOfFiling> \"" + p_country + "\" .";
            }
/*            if (p_year!=null && !p_year.isEmpty()) {
                sparql += "?pat <http://w3id.org/pmo.owl#dateOfPublication> ?p_year .";
            } */

            //RESTRICCIONES SOBRE LA EMPRESA
            if (!a_name.isEmpty() || !a_country.isEmpty() || res.equals("a"))
            {
                sparql += "?pat <http://w3id.org/pmo.owl#applicant> ?a .\n";
            }
            if (a_name!=null && !a_name.isEmpty()) {
                sparql += "?a <http://purl.org/dc/terms/title> ?aname .\n";
            }  
            if (a_country!=null && !a_country.isEmpty()) {
                sparql += "?a <http://www.w3.org/2006/vcard/ns#country-name> \"" + a_country + "\" .";
            }
             
            //RESTRICCIONES SOBRE EL INVENTOR
            if (!i_name.isEmpty() || !i_country.isEmpty() || res.equals("i"))
            {
                sparql += "?pat <http://w3id.org/pmo.owl#inventor> ?i .\n";
                sparql += "?i <http://purl.org/dc/terms/title> ?iname .\n";
           }
            if (i_name!=null && !i_name.isEmpty()) {
//                sparql += "?i <http://purl.org/dc/terms/title> ?iname .\n";
            }
            if (i_country!=null && !i_country.isEmpty()) {
                sparql += "?i <http://www.w3.org/2006/vcard/ns#country-name> \"" + i_country + "\" .";
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
//            if (p_year!=null && !p_year.isEmpty()) {
            
            
            sparql+= "} LIMIT 25\n";        
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
            
            answer = "<!-- " + sparql +" -->\n"; 
            
            //p, q, a
            String res = request.getParameter("res");
            
            
            Query query = QueryFactory.create(sparql);
            QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
            Set<String> upats = new HashSet();
            Set<String> ui = new HashSet();
            Set<String> ua = new HashSet();
            
            try {
                ResultSet results = qexec.execSelect();
                for (; results.hasNext();) {
                    QuerySolution qs = results.next();
                    
                    if (res.equals("p"))
                    {
                        String pat = qs.get("?pat").asResource().getURI();
                        String title = qs.get("?title").asLiteral().getLexicalForm();
                        int size0 = upats.size();
                        upats.add(pat);
                        int size1 = upats.size();
                        if (size1>size0)
                        {
                            answer += "<a href=\""+pat+"\">"+ title + "</a><br>\n";
                        }
                    }
                    if (res.equals("i"))
                    {
                        String i = qs.get("?i").asResource().getURI();
                        String iname = qs.get("?iname").asLiteral().getLexicalForm();
                        int size0 = ui.size();
                        ui.add(i);
                        int size1 = ui.size();
                        if (size1>size0)
                        {
                            answer += "<a href=\""+i+"\">"+ iname + "</a><br>\n";
                        }
                    }
                    if (res.equals("a"))
                    {
                        String a = qs.get("?a").asResource().getURI();
                        String aname = qs.get("?aname").asLiteral().getLexicalForm();
                        int size0 = ua.size();
                        ua.add(a);
                        int size1 = ua.size();
                        if (size1>size0)
                        {
                            answer += "<a href=\""+a+"\">"+ aname + "</a><br>\n";
                        }
                    }
                    
                    
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
