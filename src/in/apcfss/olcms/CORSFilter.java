package in.apcfss.olcms;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
/**
 * Servlet Filter implementation class CORSFilter
 */
// Enable it for Servlet 3.x implementations
/* @ WebFilter(asyncSupported = true, urlPatterns = { "/*" }) */
public class CORSFilter implements Filter {
 
  /**
   * Default constructor.
   */
  public CORSFilter() {
    // TODO Auto-generated constructor stub
  }
 
  /**
   * @see Filter#destroy()
   */
  public void destroy() {
    // TODO Auto-generated method stub
  }
 
  /**
   * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
   */
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
      throws IOException, ServletException {
 
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    System.out.println("CORSFilter HTTP Request: " + request.getMethod());
 
    HttpServletResponse response = (HttpServletResponse) servletResponse;
    // Authorize (allow) all domains to consume the content
    response.addHeader("Access-Control-Allow-Origin", "*");
    response.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
    response.addHeader("Access-Control-Allow-Headers", "X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept");
    response.addHeader("Access-Control-Max-Age", "1728000");
 
    // HttpServletResponse resp = (HttpServletResponse) servletResponse;
 
    // For HTTP OPTIONS verb/method reply with ACCEPTED status code -- per CORS handshake
    if (request.getMethod().equals("OPTIONS")) {
    	response.setStatus(HttpServletResponse.SC_ACCEPTED);
      return;
    }
 
    // pass the request along the filter chain
    chain.doFilter(request, servletResponse);
  }
 
  /**
   * @see Filter#init(FilterConfig)
   */
  public void init(FilterConfig fConfig) throws ServletException {
    // TODO Auto-generated method stub
  }
 
}
