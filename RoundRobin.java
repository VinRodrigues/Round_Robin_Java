import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class RoundRobin {
    public static void main(String[] args) throws IOException {
        // pede ao usuário para inserir o quantum
        Scanner input = new Scanner(System.in);
        System.out.print("Insira o Quantum: ");
        int quantum = input.nextInt();
        input.close();

        // lê o arquivo de entrada
        File file = new File("entrada.txt");
        Scanner scanner;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado!");
            return;
        }

        // cria lista de processos
        ArrayList<Processo> processos = new ArrayList<>();
        FileWriter escrever = new FileWriter("gantt.txt");
        // lê cada linha do arquivo e cria um processo correspondente
        while (scanner.hasNextLine()) {
            String[] line = scanner.nextLine().split(" ");
            String pid = line[0];
            int duracao = Integer.parseInt(line[1]);
            int chegada = Integer.parseInt(line[2]);
            boolean io = false;
            ArrayList<Integer> ioInstantes = new ArrayList<>();
            if (line.length > 3) {
                io = true;
                String[] ioString = line[3].split(",");
                for (String ioInstante : ioString) {
                    ioInstantes.add(Integer.parseInt(ioInstante));
                }
            }
            processos.add(new Processo(pid, duracao, chegada, io, ioInstantes));

        }
        scanner.close();

        // executa o escalonamento Round-Robin
        System.out.println("***********************************");
        System.out.println("***** ESCALONADOR ROUND ROBIN *****");
        System.out.println("-----------------------------------");
        System.out.println("------- INICIANDO SIMULAÇÃO -------");
        System.out.println("-----------------------------------");

        int tempo = 0;

        ArrayList<Processo> fila = new ArrayList<>();
        Processo cpu = null;
        System.out.println("********** TEMPO " + tempo + " **************");
        StringBuilder graficoGantt = new StringBuilder();
        StringBuilder tempos = new StringBuilder();
        StringBuilder eventos = new StringBuilder(); // Armazena os eventos
        while (!processos.isEmpty() || !fila.isEmpty()) {
            for (Processo processo : processos) {
                if (processo.chegada == tempo && tempo > 0) {
                    fila.add(processo);
                    System.out.println("#[evento] CHEGADA <" + processo.pid + ">");
                    
                    if (cpu == null) {
                        cpu = fila.remove(0);
                    }
                } else if (processo.chegada == tempo) {
                    fila.add(processo);
                    if (cpu == null) {
                        
                        cpu = fila.remove(0);
                        System.out.println("FILA: " + imprimirFila(fila, cpu));
                        System.out.println("CPU: " + cpu.toString());
                    } else {
                        
                        System.out.println("CPU: " + cpu.toString());
                    }
                }
            }

            if (cpu != null) {
                cpu.duracao--;
                graficoGantt.append(cpu.pid);
                cpu.tempoCPU++; // Atualiza o tempo que o processo ficou na CPU
                cpu.cpuTempo++; // Atualiza o tempo de CPU específico do processo

                if (cpu.duracao == 0) {
                    eventos.append("#[evento] ENCERRANDO <").append(cpu.pid).append(">");
                    cpu.tempoEspera = (tempo - cpu.chegada) - cpu.tempoCPU + 1;
                    cpu = null;
                    if (!fila.isEmpty()) {
                        cpu = fila.remove(0);
                        cpu.cpuTempo = 0; // Reseta o tempo de CPU para o novo processo

                    }
                } else {
                    if (cpu.io && !cpu.ioInstantes.isEmpty() && cpu.tempoCPU == cpu.ioInstantes.get(0)) {
                        cpu.ioInstantes.remove(0);
                        fila.add(cpu);
                        eventos.append("#[evento] OPERAÇÃO I/O <").append(cpu.pid).append(">");
                        cpu = null;

                        if (!fila.isEmpty()) {
                            cpu = fila.remove(0);
                            cpu.cpuTempo = 0; // Reseta o tempo de CPU para o novo processo
                        }
                    } else if (cpu.cpuTempo == quantum) {
                        fila.add(cpu);
                        eventos.append("#[evento] FIM QUANTUM <").append(cpu.pid).append(">");
                        cpu = null;
                        if (!fila.isEmpty()) {
                            cpu = fila.remove(0);
                            cpu.cpuTempo = 0; // Reseta o tempo de CPU para o novo processo
                        }
                    }
                }
            } else {
                System.out.println("ACABARAM OS PROCESSOS!!");
                System.out.println("-----------------------------------");
                System.out.println("------- Encerrando simulação -------");
                System.out.println("-----------------------------------");
                break;
            }

            tempo++;
            System.out.println("********** TEMPO " + tempo + " **************");
            if (eventos.length() > 0) {
                System.out.println(eventos); // Imprime os eventos
                eventos.setLength(0); // Limpa a variável de eventos
            }
            System.out.println("FILA: " + imprimirFila(fila, cpu));
            if (cpu != null) {
                System.out.println("CPU: " + cpu.toString());
            }

            // Atualiza o gráfico de Gantt e as marcações de tempo
            graficoGantt.append("|");
            tempos.append(tempo);
            if (tempo < 10) {
                tempos.append(" |");
            } else {
                tempos.append("|");
            }
        }

        // Imprime o gráfico de Gantt e as marcações de tempo
        escrever.write("\nGráfico de Gantt:\n");
        escrever.write("|" + graficoGantt.toString() + "\n");
        escrever.write("|" + tempos.toString() + "\n");

        escrever.write("\nTempo de espera dos processos: \n");
        for (Processo processo : processos) {
            escrever.write(processo.pid + ": " + processo.tempoEspera + "\n");
        }
        // Calcula o tempo médio de espera
        int totalTempoEspera = 0;
        for (Processo processo : processos) {
            totalTempoEspera += processo.tempoEspera;
        }
        double tempoMedioEspera = (double) totalTempoEspera / processos.size();
        escrever.write("Tempo Médio de Espera: " + tempoMedioEspera);
        escrever.close();
    }

    // método auxiliar para imprimir a fila de processos
    public static String imprimirFila(ArrayList<Processo> fila, Processo cpu) {
        if (fila.isEmpty() || (fila.size() == 0 && cpu != null && fila.get(0).pid.equals(cpu.pid))) {
            return "Não há processos na fila";
        }
        StringBuilder filaString = new StringBuilder();
        for (int i = 0; i < fila.size(); i++) {
            Processo processo = fila.get(i);
            if (cpu == null || !processo.pid.equals(cpu.pid)) {
                filaString.append(processo.toString()).append(" ");
            }
        }
        return filaString.toString();
    }
}

class Processo {
    String pid; // Nome do processo
    int duracao;// Duração do processo
    int chegada; // Tempo que o processo chegou
    boolean io;  // Verifica se o processo tem i\o
    ArrayList<Integer> ioInstantes; // Guarda os momentos de I\O dos processos
    int tempoCPU; // Tempo que o processo ficou na CPU
    int cpuTempo; // Tempo de CPU específico do processo
    int tempoEspera; // Tempo de espera do processo

    public Processo(String pid, int duracao, int chegada, boolean io, ArrayList<Integer> ioInstantes) {
        this.pid = pid;
        this.duracao = duracao;
        this.chegada = chegada;
        this.io = io;
        this.ioInstantes = ioInstantes;
        this.tempoCPU = 0;
        this.cpuTempo = 0;
        this.tempoEspera = 0;
    }

    @Override
    public String toString() {
        return pid + "(" + duracao + ")";
    }
}
