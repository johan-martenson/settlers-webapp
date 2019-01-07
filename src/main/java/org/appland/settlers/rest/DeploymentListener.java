package org.appland.settlers.rest;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
class DeploymentListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        System.out.println("Context initialized event.");

        GameTicker gameTicker = new GameTicker();

        servletContextEvent.getServletContext().setAttribute("gameTicker", gameTicker);

        gameTicker.activate();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        System.out.println("Context destroyed event");

        GameTicker gameTicker = (GameTicker) servletContextEvent.getServletContext().getAttribute("gameTicker");

        gameTicker.deactivate();
    }
}
