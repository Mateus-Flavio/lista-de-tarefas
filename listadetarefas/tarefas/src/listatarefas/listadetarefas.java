package listatarefas;

import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class listadetarefas extends Application {

    private ObservableList<Tarefa> tasks;
    private final Path arquivoTarefas = Path.of("tarefas.txt");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Lista de Tarefas");

        tasks = FXCollections.observableArrayList();
        carregarTarefas();

        TextField taskInput = new TextField();
        taskInput.setPromptText("Digite uma nova tarefa");

        Button addButton = new Button("Adicionar");
        addButton.setOnAction(e -> {
            String descricao = taskInput.getText().trim();
            if (descricao.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Entrada Inválida", "A tarefa não pode ser vazia.");
            } else if (tasks.stream().anyMatch(t -> t.getDescricao().equalsIgnoreCase(descricao))) {
                showAlert(Alert.AlertType.WARNING, "Duplicado", "Esta tarefa já existe.");
            } else {
                Tarefa nova = new Tarefa(descricao, false);
                tasks.add(nova);
                taskInput.clear();
                salvarTarefas();
            }
        });

        ListView<Tarefa> taskListView = new ListView<>(tasks);
        taskListView.setPrefHeight(250);
        taskListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        taskListView.setEditable(true);

        // Mostrar checkbox e texto riscado se concluída
        taskListView.setCellFactory(CheckBoxListCell.forListView(
                Tarefa::concluidaProperty,
                new StringConverter<Tarefa>() {
                    @Override
                    public String toString(Tarefa tarefa) {
                        return tarefa.getDescricao();
                    }

                    @Override
                    public Tarefa fromString(String string) {
                        return null;
                    }
                }
        ));
        taskListView.setCellFactory(lv -> {
    ListCell<Tarefa> cell = new CheckBoxListCell<>(Tarefa::concluidaProperty, new StringConverter<Tarefa>() {
        @Override
        public String toString(Tarefa tarefa) {
            return tarefa.getDescricao();
        }

        @Override
        public Tarefa fromString(String string) {
            return null;
        }
    });

    cell.setOnMouseClicked(event -> {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !cell.isEmpty()) {
            Tarefa tarefa = cell.getItem();
            TextInputDialog dialog = new TextInputDialog(tarefa.getDescricao());
            dialog.setTitle("Editar Tarefa");
            dialog.setHeaderText(null);
            dialog.setContentText("Nova descrição:");
            dialog.showAndWait().ifPresent(novaDescricaoOriginal -> {
                String novaDescricao = novaDescricaoOriginal.trim();
                if (novaDescricao.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Entrada Inválida", "Descrição não pode ser vazia.");
                } else if (tasks.stream().anyMatch(t -> t.getDescricao().equalsIgnoreCase(novaDescricao) && t != tarefa)) {
                    showAlert(Alert.AlertType.WARNING, "Duplicado", "Já existe uma tarefa com essa descrição.");
                } else {
                    tarefa.setDescricao(novaDescricao);
                    taskListView.refresh();
                    salvarTarefas();
                }
            });
        }
    });

    // Atualiza o estilo do texto riscado conforme a tarefa estiver concluída
    cell.itemProperty().addListener((obs, oldItem, newItem) -> {
        if (newItem != null) {
            newItem.concluidaProperty().addListener((o, oldVal, newVal) -> {
                atualizarEstilo(cell, newVal);
                salvarTarefas();
            });
            atualizarEstilo(cell, newItem.isConcluida());
        }
    });

    return cell;
});

        

        Button removeButton = new Button("Remover Selecionadas");
        removeButton.setOnAction(e -> {
            List<Tarefa> selecionadas = new ArrayList<>(taskListView.getSelectionModel().getSelectedItems());
            if (selecionadas.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Nenhuma Seleção", "Selecione ao menos uma tarefa para remover.");
            } else {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Deseja remover as tarefas selecionadas?", ButtonType.YES, ButtonType.NO);
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        tasks.removeAll(selecionadas);
                        salvarTarefas();
                    }
                });
            }
        });

        HBox inputLayout = new HBox(10, taskInput, addButton);
        VBox mainLayout = new VBox(10, inputLayout, taskListView, removeButton);
        mainLayout.setPadding(new Insets(10));

        Scene scene = new Scene(mainLayout, 450, 400);
        scene.getStylesheets().add(getClass().getResource("estilo.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void atualizarEstilo(ListCell<Tarefa> cell, boolean concluida) {
        if (concluida) {
            cell.setStyle("-fx-text-fill: gray; -fx-strikethrough: true;");
        } else {
            cell.setStyle("-fx-text-fill: black; -fx-strikethrough: false;");
        }
    }

    private void showAlert(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void salvarTarefas() {
        try (BufferedWriter writer = Files.newBufferedWriter(arquivoTarefas)) {
            for (Tarefa t : tasks) {
                // Formato simples: concluida|descricao
                writer.write(t.isConcluida() + "|" + t.getDescricao());
                writer.newLine();
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível salvar as tarefas.");
        }
    }

    private void carregarTarefas() {
        if (!Files.exists(arquivoTarefas)) return;

        try (BufferedReader reader = Files.newBufferedReader(arquivoTarefas)) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split("\\|", 2);
                if (partes.length == 2) {
                    boolean concluida = Boolean.parseBoolean(partes[0]);
                    String descricao = partes[1];
                    tasks.add(new Tarefa(descricao, concluida));
                }
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar as tarefas.");
        }
    }

    // Classe Tarefa com propriedades JavaFX para bind com CheckBoxListCell
    public static class Tarefa {
        private final BooleanProperty concluida;
        private String descricao;

        public Tarefa(String descricao, boolean concluida) {
            this.descricao = descricao;
            this.concluida = new SimpleBooleanProperty(concluida);
        }

        public boolean isConcluida() {
            return concluida.get();
        }

        public void setConcluida(boolean concluida) {
            this.concluida.set(concluida);
        }

        public BooleanProperty concluidaProperty() {
            return concluida;
        }

        public String getDescricao() {
            return descricao;
        }

        public void setDescricao(String descricao) {
            this.descricao = descricao;
        }
    }
}

