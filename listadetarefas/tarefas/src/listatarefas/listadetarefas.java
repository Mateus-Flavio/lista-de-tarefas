package listatarefas;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class listadetarefas extends Application {

    private ObservableList<String> tasks;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Lista de Tarefas");

        tasks = FXCollections.observableArrayList();

        // Campo para digitar a tarefa
        TextField taskInput = new TextField();
        taskInput.setPromptText("Digite uma nova tarefa");

        // Botão para adicionar tarefa
        Button addButton = new Button("Adicionar");
        addButton.setOnAction(e -> {
            String task = taskInput.getText().trim();
            if (!task.isEmpty()) {
                tasks.add(task);
                taskInput.clear();
            }
        });

        // Lista que mostra as tarefas
        ListView<String> taskListView = new ListView<>(tasks);
        taskListView.setPrefHeight(200);

        // Botão para remover tarefa selecionada
        Button removeButton = new Button("Remover Selecionada");
        removeButton.setOnAction(e -> {
            String selected = taskListView.getSelectionModel().getSelectedItem();
            tasks.remove(selected);
        });

        // Layout do input e botão de adicionar
        HBox inputLayout = new HBox(10);
        inputLayout.getChildren().addAll(taskInput, addButton);

        // Layout principal
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(10));
        mainLayout.getChildren().addAll(inputLayout, taskListView, removeButton);

        Scene scene = new Scene(mainLayout, 350, 300);
        scene.getStylesheets().add(getClass().getResource("estilo.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
