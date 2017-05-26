package oeg.pubbyex;

import de.fuberlin.wiwiss.pubby.Configuration;
import de.fuberlin.wiwiss.pubby.servlets.BaseServlet;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * http://patents.linkeddata.es/data/api/test
 * @author Victor
 */
public class Servicio extends BaseServlet {

	public boolean doGet(String relativeURI,
			HttpServletRequest request,
			HttpServletResponse response,
			Configuration config) throws ServletException, IOException {

                String value = request.getParameter("uri");

                if (value==null)
                    value="";
                response.setContentType("text/plain;charset=UTF-8");
                PrintWriter out = response.getWriter();
		out.println("pong, " + value);

		return true;
	}
    
}
