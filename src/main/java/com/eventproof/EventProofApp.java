package com.eventproof;

import com.eventproof.dao.EventDAO;
import com.eventproof.dao.VenueDAO;
import com.eventproof.model.Event;
import com.eventproof.model.Venue;
import com.eventproof.ui.EventsView;
import com.eventproof.ui.IncidentsView;
import com.eventproof.ui.ReliabilityView;
import com.eventproof.ui.UiMessages;

import javafx.application.Application;
import java.sql.SQLException;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class EventProofApp extends Application {
    private EventsView eventsView;
    private IncidentsView incidentsView;
    private ReliabilityView reliabilityView;
    private final Label status = new Label();

    @Override
    public void start(Stage stage) {
        Label title = new Label("EventProof");
        title.getStyleClass().add("app-title");
        VBox header = new VBox(5, title, new Label("Event Reliability Management System"));
        header.setPadding(new Insets(20, 24, 18, 24));
        header.getStyleClass().add("header");
        eventsView = new EventsView(this::refresh);
        incidentsView = new IncidentsView();
        reliabilityView = new ReliabilityView();
        ScrollPane reliabilityScroll = new ScrollPane(reliabilityView);
        reliabilityScroll.setId("reliability-scroll");
        reliabilityScroll.setFitToWidth(true);
        reliabilityScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        reliabilityScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        reliabilityScroll.setStyle("-fx-background-color: transparent; -fx-background-insets: 0;");
        TabPane tabs = new TabPane(new Tab("EVENTS", eventsView), new Tab("INCIDENTS", incidentsView),
                new Tab("RELIABILITY", reliabilityScroll));
        tabs.setId("main-tabs");
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        status.setPadding(new Insets(10, 24, 10, 24));
        BorderPane content = new BorderPane(tabs, header, null, status, null);
        Scene scene = new Scene(content, 1080, 780);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setTitle("EventProof");
        stage.setMinWidth(960);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();
        refresh();
    }

    private void refresh() {
        try {
            List<Event> events = new EventDAO().listAll();
            List<Venue> venues = new VenueDAO().listAll();
            eventsView.setData(events, venues);
            incidentsView.setEvents(events);
            reliabilityView.setEvents(events);
            status.setText("MySQL connected • " + events.size() + " events • " + venues.size() + " venues");
        } catch (SQLException exception) {
            status.setText("Database unavailable. Check local configuration and run database setup, then Refresh.");
            UiMessages.databaseError(exception);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
