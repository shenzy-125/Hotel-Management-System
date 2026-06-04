module com.raahul.hms {

    requires javafx.controls;
    requires javafx.fxml;

    opens com.raahul.hms to javafx.graphics;

    opens com.raahul.hms.controller to javafx.fxml;

    opens com.raahul.hms.model to javafx.base;

    opens com.raahul.hms.ui to javafx.fxml;

    exports com.raahul.hms;
    exports com.raahul.hms.model;
    exports com.raahul.hms.service;
    exports com.raahul.hms.ui;
    exports com.raahul.hms.controller;
    exports com.raahul.hms.thread;
}
