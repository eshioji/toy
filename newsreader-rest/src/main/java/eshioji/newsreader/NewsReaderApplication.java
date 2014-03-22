package eshioji.newsreader;

import eshioji.newsreader.rest.auth.ExampleAuthenticator;
import eshioji.newsreader.tool.RenderCommand;
import eshioji.newsreader.model.Person;
import eshioji.newsreader.model.Template;
import eshioji.newsreader.dao.PersonDAO;
import eshioji.newsreader.health.TemplateHealthCheck;
import eshioji.newsreader.rest.resources.*;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.basic.BasicAuthProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;

public class NewsReaderApplication extends Application<NewsReaderConfiguration> {
    public static void main(String[] args) throws Exception {
        new NewsReaderApplication().run(args);
    }

    private final HibernateBundle<NewsReaderConfiguration> hibernateBundle =
            new HibernateBundle<NewsReaderConfiguration>(Person.class) {
                @Override
                public DataSourceFactory getDataSourceFactory(NewsReaderConfiguration configuration) {
                    return configuration.getDataSourceFactory();
                }
            };

    @Override
    public String getName() {
        return "hello-world";
    }

    @Override
    public void initialize(Bootstrap<NewsReaderConfiguration> bootstrap) {
        bootstrap.addCommand(new RenderCommand());
        bootstrap.addBundle(new AssetsBundle());
        bootstrap.addBundle(hibernateBundle);
        bootstrap.addBundle(new ViewBundle());
    }

    @Override
    public void run(NewsReaderConfiguration configuration,
                    Environment environment) throws ClassNotFoundException {
        final PersonDAO dao = new PersonDAO(hibernateBundle.getSessionFactory());
        final Template template = configuration.buildTemplate();

        environment.healthChecks().register("template", new TemplateHealthCheck(template));

        environment.jersey().register(new BasicAuthProvider<>(new ExampleAuthenticator(),
                                                              "SUPER SECRET STUFF"));
        environment.jersey().register(new HelloWorldResource(template));
        environment.jersey().register(new ViewResource());
        environment.jersey().register(new ProtectedResource());
        environment.jersey().register(new PeopleResource(dao));
        environment.jersey().register(new PersonResource(dao));
    }
}
