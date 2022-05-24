package co.com.foscal.ppa;

import co.com.foscal.FrmPrincipal;
import co.com.foscal.entidad.Capa;
import co.com.foscal.entidad.CapaLineas;
import co.com.foscal.entidad.CapaPoligonos;
import co.com.foscal.entidad.CapaPuntos;
import co.com.foscal.entidad.CoordenadaUTM;
import co.com.foscal.entidad.EntradaGenerica;
import co.com.foscal.entidad.FuncionNucleo;
import co.com.foscal.entidad.Linea;
import co.com.foscal.entidad.PartePoligono;
import co.com.foscal.entidad.Pixel;
import co.com.foscal.entidad.Poligono;
import co.com.foscal.entidad.Punto;
import co.com.foscal.procesos.PrCalculoKDE;
import co.com.foscal.utilidades.Utilidades;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 * Formulario para mostrar resultados de KDE
 *
 * @author Feisar Moreno
 * @date 10/08/2020
 */
public class FrmMostrarKDE extends JInternalFrame {

    private final FrmPrincipal frmPrincipal;
    private final FrmMostrarKDE frmMostrarKDE;
    private EntradaGenerica datosCapaEventos;
    private double anchoBanda;
    private double resolucion;
    private FuncionNucleo funcionNucleo;
    private final LinkedHashMap<String, Capa> mapaCapasKML = new LinkedHashMap<>();
    private Pixel[][] arrPixels;
    private VisorMapa visorMapa;
    private final int cantDivisiones;
    
    private class AvanceCalculo implements Runnable {

        private boolean controlCorrer = true;
        private int cantPuntosAvance = 0;

        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
            while (this.controlCorrer) {
                try {
                    Thread.sleep(500);
                    this.mostrarAvance();
                } catch (InterruptedException e) {
                }
            }
        }

        public void setControlCorrer(boolean controlCorrer) {
            this.controlCorrer = controlCorrer;
        }

        private void mostrarAvance() {
            String mensajeAvance;

            this.cantPuntosAvance++;
            this.cantPuntosAvance = this.cantPuntosAvance % 3;
            mensajeAvance = "Processing" + "....".substring(0, this.cantPuntosAvance + 1);
            lblAvance.setText(mensajeAvance);
        }
    }
    
    /**
     * Constructor para el formulario FrmMostrarKDE
     *
     * @param frmPrincipal Formulario principal
     */
    public FrmMostrarKDE(FrmPrincipal frmPrincipal) {
        initComponents();
        
        this.frmPrincipal = frmPrincipal;
        this.frmMostrarKDE = this;
        this.cantDivisiones = 10;
        
        //Se limpia el texto de avance de carga
        this.lblAvance.setVisible(false);
        this.lblAvance.setText(" ");

        //Se carga el combo de funciones de núcleo
        ArrayList<FuncionNucleo> listaFuncionesNucleo = new ArrayList<>();
        listaFuncionesNucleo.add(new FuncionNucleo(1, "Función Gaussiana"));
        listaFuncionesNucleo.add(new FuncionNucleo(2, "Función de Epanechnikov"));
        listaFuncionesNucleo.add(new FuncionNucleo(3, "Función de Varianza Mínima"));
        listaFuncionesNucleo.add(new FuncionNucleo(4, "Función Uniforme"));
        listaFuncionesNucleo.add(new FuncionNucleo(5, "Función Triangular"));

        this.cmbFuncionNucleo.removeAllItems();
        for (FuncionNucleo funcionNucleoAux : listaFuncionesNucleo) {
            this.cmbFuncionNucleo.addItem(funcionNucleoAux);
        }
        this.cmbFuncionNucleo.setSelectedIndex(-1);
    }
    
    private char obtenerTipoCapa(String linea) {
        linea = linea.toLowerCase();
        if (linea.contains("<point>")) {
            return 'p';
        } else if (linea.contains("<linestring>")) {
            return 'l';
        } else if (linea.contains("<polygon>")) {
            return 'a';
        }
        return ' ';
    }

    private Capa cargarObjetoCapa(Capa capa, char tipoCapa, String texto, String nombreCapa, String descCapa) {
        texto = texto.toLowerCase();
        if (capa == null) {
            switch (tipoCapa) {
                case 'p': //Puntos
                    capa = new CapaPuntos(nombreCapa, descCapa);
                    break;
                case 'l': //Lineas
                    capa = new CapaLineas(nombreCapa, descCapa);
                    break;
                case 'a': //Polígonos
                    capa = new CapaPoligonos(nombreCapa, descCapa);
                    break;
            }
        }

        if (capa != null) {
            int posIni;
            int posFin;
            switch (tipoCapa) {
                case 'p': //Puntos
                    CapaPuntos capaPuntos = (CapaPuntos) capa;
                    posIni = texto.indexOf("<coordinates>");
                    posFin = texto.indexOf("</coordinates>");
                    if (posIni >= 0 && posIni < posFin) {
                        String[] arrAux = texto.substring(posIni + 13, posFin).split(",");
                        double latitud;
                        double longitud;
                        try {
                            latitud = Double.parseDouble(arrAux[1]);
                            longitud = Double.parseDouble(arrAux[0]);
                        } catch (NumberFormatException ex) {
                            latitud = Double.NaN;
                            longitud = Double.NaN;
                        }

                        if (!Double.isNaN(latitud) && !Double.isNaN(longitud)) {
                            Punto punto = new Punto(capaPuntos.getIdCapa(), capaPuntos.getCantPuntos(), latitud, longitud);
                            capaPuntos.addPunto(punto);
                        }
                    }
                    break;

                case 'l': //Lineas
                    CapaLineas capaLineas = (CapaLineas) capa;
                    do {
                        posIni = texto.indexOf("<coordinates>");
                        posFin = texto.indexOf("</coordinates>");
                        if (posIni >= 0 && posIni < posFin) {
                            String[] arrCoordenadas = texto.substring(posIni + 13, posFin).split("\\s");
                            if (arrCoordenadas.length > 0) {
                                Linea linea = new Linea(capaLineas, capaLineas.getCantLineas());
                                capaLineas.addLinea(linea);
                                for (String valAux : arrCoordenadas) {
                                    String[] arrAux = valAux.split(",");
                                    double latitud;
                                    double longitud;
                                    try {
                                        latitud = Double.parseDouble(arrAux[1]);
                                        longitud = Double.parseDouble(arrAux[0]);
                                    } catch (NumberFormatException ex) {
                                        latitud = Double.NaN;
                                        longitud = Double.NaN;
                                    }

                                    if (!Double.isNaN(latitud) && !Double.isNaN(longitud)) {
                                        Punto punto = new Punto(linea.getIdCapa(), linea.getCantPuntos(), latitud, longitud);
                                        linea.addPunto(punto);
                                    }
                                }
                            }

                            texto = texto.substring(posFin + 14);
                        } else {
                            break;
                        }
                    } while (posIni >= 0);
                    break;

                case 'a': //Polígonos
                    CapaPoligonos capaPoligonos = (CapaPoligonos) capa;
                    Poligono poligono = new Poligono(capaPoligonos, capaPoligonos.getCantPoligonos());
                    capaPoligonos.addPoligono(poligono);
                    do {
                        posIni = texto.indexOf("<coordinates>");
                        posFin = texto.indexOf("</coordinates>");
                        int posExterno = texto.indexOf("<outerboundaryis>");
                        int posInterno = texto.indexOf("<innerboundaryis>");
                        char tipoParte = ' ';
                        if (posExterno >= 0 && posExterno < posIni) {
                            tipoParte = 'e';
                        } else if (posInterno >= 0 && posInterno < posIni) {
                            tipoParte = 'i';
                        }
                        if (posIni >= 0 && posIni < posFin && tipoParte != ' ') {
                            String[] arrCoordenadas = texto.substring(posIni + 13, posFin).split("\\s");
                            if (arrCoordenadas.length > 0) {
                                PartePoligono partePoligono = new PartePoligono(capaPoligonos, poligono.getCantPartes(), tipoParte);
                                poligono.addParte(partePoligono);
                                for (String valAux : arrCoordenadas) {
                                    String[] arrAux = valAux.split(",");
                                    double latitud;
                                    double longitud;
                                    try {
                                        latitud = Double.parseDouble(arrAux[1]);
                                        longitud = Double.parseDouble(arrAux[0]);
                                    } catch (NumberFormatException ex) {
                                        latitud = Double.NaN;
                                        longitud = Double.NaN;
                                    }

                                    if (!Double.isNaN(latitud) && !Double.isNaN(longitud)) {
                                        Punto punto = new Punto(poligono.getIdCapa(), partePoligono.getCantPuntos(), latitud, longitud);
                                        partePoligono.addPunto(punto);
                                    }
                                }
                            }

                            texto = texto.substring(posFin + 14);
                        } else {
                            break;
                        }
                    } while (posIni >= 0);
                    break;
            }
        }

        return capa;
    }
    
    private void cargarTablaKML() {
        //Nombres de las columnas
        String[] nombCols = new String[2];
        nombCols[0] = "Capa";
        nombCols[1] = "Borrar";

        //Se cargan los registros de archivos KML
        String[] arrRutas = new String[this.mapaCapasKML.size()];
        String[][] cuerpoTabla = new String[this.mapaCapasKML.size()][0];
        int cont = 0;
        for (Map.Entry<String, Capa> entradaAux : this.mapaCapasKML.entrySet()) {
            arrRutas[cont] = entradaAux.getKey();

            String[] registroAux = new String[2];
            registroAux[0] = "" + entradaAux.getValue().getDescCapa();
            registroAux[1] = " ... ";

            cuerpoTabla[cont] = registroAux;
            cont++;
        }

        DefaultTableModel tablaKML = new DefaultTableModel(cuerpoTabla, nombCols) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnIndex == 1;
            }
        };

        this.tblKML.setModel(tablaKML);
        TableColumnModel columnas = this.tblKML.getColumnModel();
        columnas.getColumn(0).setPreferredWidth(305);
        columnas.getColumn(1).setPreferredWidth(50);

        FrmMostrarKDE.ButtonRendererKDE brKDE = new FrmMostrarKDE.ButtonRendererKDE();
        brKDE.setOpaque();
        this.tblKML.getColumn("Borrar").setCellRenderer(brKDE);
        this.tblKML.getColumn("Borrar").setCellEditor(
                new FrmMostrarKDE.ButtonEditorKDE(new JCheckBox(), this.tblKML, arrRutas)
        );
    }

    private void actualizarComboCapas() {
        EntradaGenerica egAnt;
        Object objAux = this.cmbCapaEventos.getSelectedItem();
        if (objAux != null) {
            egAnt = (EntradaGenerica) objAux;
        } else {
            egAnt = null;
        }
        this.cmbCapaEventos.removeAllItems();
        for (Map.Entry<String, Capa> entradaAux : this.mapaCapasKML.entrySet()) {
            if (entradaAux.getValue().getTipoCapa() == 'p') {
                EntradaGenerica egAux = new EntradaGenerica(entradaAux.getKey(), entradaAux.getValue().getDescCapa());
                this.cmbCapaEventos.addItem(egAux);
            }
        }
        if (egAnt != null) {
            this.cmbCapaEventos.setSelectedItem(egAnt);
        } else {
            this.cmbCapaEventos.setSelectedIndex(-1);
        }
    }
    
    /**
     * Método que muestra los resultados del método KDE.
     */
    public void mostrarResultados() {
        this.addInternalFrameListener(new FrmMostrarKDEListener());

        //Se agrega el lienzo al panel
        this.panContenedor.setLayout(new java.awt.GridLayout(1, 1));
        this.panContenedor.removeAll();
        
        if (this.cmbCapaEventos.getSelectedIndex() >= 0) {
            this.datosCapaEventos = (EntradaGenerica) cmbCapaEventos.getSelectedItem();
        } else {
            this.datosCapaEventos = null;
        }
        
        this.visorMapa = new VisorMapa(this.anchoBanda, this.resolucion, this.mapaCapasKML, this.datosCapaEventos, this.arrPixels, this.cantDivisiones);
        this.panResultados.removeAll();
        this.panResultados.add(visorMapa);
        this.panContenedor.add(this.panResultados);

        this.panResultados.setSize(this.panContenedor.getSize());
        this.visorMapa.setSize(this.panResultados.getSize());
    }

    private File crearImagenPNGResultado(String nombreArchivoPNG) throws IOException {
        //Se asignan las dimensiones de la imagen, de acuerdo con el número de pixels del resultado
        int ancho = this.arrPixels[0].length;
        int alto = this.arrPixels.length;

        BufferedImage bufferedImage = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics();

        //La imagen se llena por defecto con una transparencia
        g2d.setColor(new Color(255, 255, 255, 0));
        g2d.fillRect(0, 0, ancho, alto);

        //Se hallan los colores de los pixels y las divisiones
        Color[] arrColores = this.visorMapa.getArrColores();
        double[] arrDivisiones = this.visorMapa.getArrDivisiones();

        //Se pinta cada color del resultado
        for (int i = 0; i < this.arrPixels.length; i++) {
            for (int j = 0; j < this.arrPixels[i].length; j++) {
                Pixel pixel = this.arrPixels[i][j];
                if (pixel.getDensidad() > 0) {
                    //Se determina el valor del pixel de acuerdo a los deciles
                    int decilAux = this.cantDivisiones - 1;
                    for (int k = 0; k < this.cantDivisiones; k++) {
                        if (pixel.getDensidad() <= arrDivisiones[k]) {
                            decilAux = k;
                            break;
                        }
                    }
                    Color colorAux = arrColores[decilAux];
                    g2d.setColor(new Color(colorAux.getRed(), colorAux.getGreen(), colorAux.getBlue(), 192));
                    g2d.fillRect(j, i, 1, 1);
                }
            }
        }

        g2d.dispose();

        //Se crea el archivo PNG
        File archivoPNG = new File(nombreArchivoPNG);
        ImageIO.write(bufferedImage, "png", archivoPNG);

        return archivoPNG;
    }
    
    private File crearArchivoKMLResultado(String nombreArchivoKML, String nombreArchivoPNG) throws IOException {
        //Se hallan las coordenadas de los extremos de la imagen
        double minXAux = this.arrPixels[0][0].getxCentro() - this.resolucion / 2;
        double minYAux = this.arrPixels[this.arrPixels.length - 1][this.arrPixels[0].length - 1].getyCentro() - this.resolucion / 2;
        double maxXAux = this.arrPixels[this.arrPixels.length - 1][this.arrPixels[0].length - 1].getxCentro() + this.resolucion / 2;
        double maxYAux = this.arrPixels[0][0].getyCentro() + this.resolucion / 2;
        
        CapaPuntos capaEventos = (CapaPuntos) this.mapaCapasKML.get(this.datosCapaEventos.getLlave());
        CoordenadaUTM utmAux = capaEventos.getMapaPuntos().get(0L).getCoordenadaUTM();
        
        double[] arrAux = Utilidades.convertirUTMAGeograficas(new CoordenadaUTM(minXAux, minYAux, utmAux.getZona(), utmAux.getHemisferio()));
        double latSur = arrAux[0];
        double lonOeste = arrAux[1];
        arrAux = Utilidades.convertirUTMAGeograficas(new CoordenadaUTM(maxXAux, maxYAux, utmAux.getZona(), utmAux.getHemisferio()));
        double latNorte = arrAux[0];
        double lonEste = arrAux[1];
        
        //Se halla el nombre sin ruta del archivo PNG asociado
        int posAux = nombreArchivoPNG.lastIndexOf(File.separator);
        if (posAux >= 0) {
            nombreArchivoPNG = nombreArchivoPNG.substring(posAux + 1);
        }

        File archivoKML = new File(nombreArchivoKML);

        try (PrintWriter pw = new PrintWriter(archivoKML)) {
            //Se encribe el contenido del archivo KML
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\">");
            pw.println("    <Document>");
            pw.println("        <name>" + nombreArchivoKML + "</name>");
            pw.println("        <styleUrl>#hideChildrenStyle</styleUrl>");
            pw.println("        <Style id=\"hideChildrenStyle\">");
            pw.println("            <ListStyle id=\"hideChildren\">");
            pw.println("                <listItemType>checkHideChildren</listItemType>");
            pw.println("            </ListStyle>");
            pw.println("        </Style>");
            pw.println("        <Region>");
            pw.println("            <LatLonAltBox>");
            pw.println("                <north>" + latNorte + "</north>");
            pw.println("                <south>" + latSur + "</south>");
            pw.println("                <east>" + lonEste + "</east>");
            pw.println("                <west>" + lonOeste + "</west>");
            pw.println("            </LatLonAltBox>");
            pw.println("            <Lod>");
            pw.println("                <minLodPixels>1</minLodPixels>");
            pw.println("                <maxLodPixels>-1</maxLodPixels>");
            pw.println("            </Lod>");
            pw.println("        </Region>");
            pw.println("        <GroundOverlay>");
            pw.println("            <drawOrder>0</drawOrder>");
            pw.println("            <Icon>");
            pw.println("                <href>" + nombreArchivoPNG + "</href>");
            pw.println("            </Icon>");
            pw.println("            <LatLonBox>");
            pw.println("                <north>" + latNorte + "</north>");
            pw.println("                <south>" + latSur + "</south>");
            pw.println("                <east>" + lonEste + "</east>");
            pw.println("                <west>" + lonOeste + "</west>");
            pw.println("            </LatLonBox>");
            pw.println("        </GroundOverlay>");
            pw.println("    </Document>");
            pw.println("</kml>");
        }

        return archivoKML;
    }

    private boolean validarCamposCalculo() {
        if (this.cmbCapaEventos.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this.frmPrincipal, "Debe seleccionar una capa de eventos.", "Error", JOptionPane.ERROR_MESSAGE);
            this.cmbCapaEventos.requestFocusInWindow();
            return false;
        }

        if (this.txtAnchoBanda.getText().equals("")) {
            JOptionPane.showMessageDialog(this.frmPrincipal, "Debe seleccionar un ancho de banda.", "Error", JOptionPane.ERROR_MESSAGE);
            this.txtAnchoBanda.requestFocusInWindow();
            return false;
        } else {
            //Se valida que se haya ingresado un número
            try {
                Double.parseDouble(this.txtAnchoBanda.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this.frmPrincipal, "El ancho de banda debe ser un valor numérico.", "Error", JOptionPane.ERROR_MESSAGE);
                this.txtAnchoBanda.requestFocusInWindow();
                return false;
            }
        }

        if (this.cmbFuncionNucleo.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this.frmPrincipal, "Debe seleccionar una función de núcleo.", "Error", JOptionPane.ERROR_MESSAGE);
            this.cmbFuncionNucleo.requestFocusInWindow();
            return false;
        }

        if (this.txtResolucion.getText().equals("")) {
            JOptionPane.showMessageDialog(this.frmPrincipal, "Debe seleccionar una resolución.", "Error", JOptionPane.ERROR_MESSAGE);
            this.txtResolucion.requestFocusInWindow();
            return false;
        } else {
            //Se valida que se haya ingresado un número
            try {
                Double.parseDouble(this.txtResolucion.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this.frmPrincipal, "La resolución debe ser un valor numérico.", "Error", JOptionPane.ERROR_MESSAGE);
                this.txtResolucion.requestFocusInWindow();
                return false;
            }
        }
        
        return true;
    }
    
    public void habilitarComponentes(boolean habilitar) {
        this.txtAnchoBanda.setEnabled(true);
        this.txtResolucion.setEnabled(true);
        this.cmbCapaEventos.setEnabled(true);
        this.cmbFuncionNucleo.setEnabled(true);
        this.chkLeyenda.setEnabled(true);
        this.btnBuscarArchivo.setEnabled(true);
        this.btnCalcular.setEnabled(true);
        this.btnExportarKDE.setEnabled(true);
        if (!habilitar) {
            this.txtAnchoBanda.setEnabled(false);
            this.txtResolucion.setEnabled(false);
            this.cmbCapaEventos.setEnabled(false);
            this.cmbFuncionNucleo.setEnabled(false);
            this.chkLeyenda.setEnabled(false);
            this.btnBuscarArchivo.setEnabled(false);
            this.btnCalcular.setEnabled(false);
            this.btnExportarKDE.setEnabled(false);
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        chkLeyenda = new javax.swing.JCheckBox();
        panContenedor = new javax.swing.JPanel();
        panResultados = new javax.swing.JPanel();
        btnExportarKDE = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        btnBuscarArchivo = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblKML = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        cmbCapaEventos = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        cmbFuncionNucleo = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        txtAnchoBanda = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtResolucion = new javax.swing.JTextField();
        btnCalcular = new javax.swing.JButton();
        lblAvance = new javax.swing.JLabel();

        setClosable(true);
        setTitle("Kernel Density Estimation - KDE");

        chkLeyenda.setText("Ver leyenda de datos");
        chkLeyenda.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chkLeyendaItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkLeyenda)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkLeyenda)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panContenedor.setBackground(new java.awt.Color(255, 255, 255));
        panContenedor.setLayout(new java.awt.GridLayout(1, 2, 5, 0));

        panResultados.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout panResultadosLayout = new javax.swing.GroupLayout(panResultados);
        panResultados.setLayout(panResultadosLayout);
        panResultadosLayout.setHorizontalGroup(
            panResultadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 472, Short.MAX_VALUE)
        );
        panResultadosLayout.setVerticalGroup(
            panResultadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 686, Short.MAX_VALUE)
        );

        panContenedor.add(panResultados);

        btnExportarKDE.setText("Exportar resultado KDE");
        btnExportarKDE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportarKDEActionPerformed(evt);
            }
        });

        btnBuscarArchivo.setText("Agregar archivo KML");
        btnBuscarArchivo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarArchivoActionPerformed(evt);
            }
        });

        tblKML.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        tblKML.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jScrollPane4.setViewportView(tblKML);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btnBuscarArchivo)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnBuscarArchivo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel11.setText("Capa de eventos");

        jLabel9.setText("Función de núcleo");

        jLabel2.setText("Ancho de banda (metros)");

        txtAnchoBanda.setToolTipText("");

        jLabel6.setText("Resolución (metros)");

        txtResolucion.setToolTipText("");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel11)
                    .addComponent(jLabel9)
                    .addComponent(cmbCapaEventos, 0, 155, Short.MAX_VALUE)
                    .addComponent(cmbFuncionNucleo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 50, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(txtAnchoBanda, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(txtResolucion, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbCapaEventos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtAnchoBanda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbFuncionNucleo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtResolucion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnCalcular.setText("Calcular");
        btnCalcular.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCalcularActionPerformed(evt);
            }
        });

        lblAvance.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        lblAvance.setText(".");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnCalcular)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblAvance, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(btnExportarKDE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panContenedor, javax.swing.GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panContenedor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnCalcular)
                            .addComponent(lblAvance))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnExportarKDE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void chkLeyendaItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkLeyendaItemStateChanged
        //Se verifica si se debe mostrar o no la leyenda
        this.visorMapa.setIndVerLeyenda(this.chkLeyenda.isSelected());
        this.visorMapa.repaint();
    }//GEN-LAST:event_chkLeyendaItemStateChanged

    private void btnExportarKDEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportarKDEActionPerformed
        if (this.arrPixels == null) {
            JOptionPane.showMessageDialog(this.frmPrincipal, "Aún no se han realizado cálculos del método KDE.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        //Se carga la ruta base de búsqueda
        String rutaBase = Utilidades.obtenerRutaBase();

        //Se pregunta la ruta en la que se guardará el archivo KMZ
        JFileChooser fileChooser = new JFileChooser();
        if (!rutaBase.equals("")) {
            fileChooser.setCurrentDirectory(new File(rutaBase));
        }
        fileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public String getDescription() {
                return "Archivo KMl (*.kml)";
            }
            
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                } else {
                    return f.getName().toLowerCase().endsWith(".kmz");
                }
            }
        });
        
        int retornoSel = fileChooser.showSaveDialog(this);
        if (retornoSel == JFileChooser.APPROVE_OPTION) {
            String nombreArchivoPNG = fileChooser.getSelectedFile().getAbsolutePath() + ".png";
            String nombreArchivoKML = fileChooser.getSelectedFile().getAbsolutePath() + ".kml";

            try {
                //Se crea la imagen de resultado
                this.crearImagenPNGResultado(nombreArchivoPNG);

                //Se crea el archivo KML de resultado
                this.crearArchivoKMLResultado(nombreArchivoKML, nombreArchivoPNG);
            } catch (IOException ex) {
            }
        }
    }//GEN-LAST:event_btnExportarKDEActionPerformed

    private void btnBuscarArchivoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarArchivoActionPerformed
        //Se carga la ruta base de búsqueda
        String rutaBaseAnt = Utilidades.obtenerRutaBase();

        JFileChooser fileChooser = new JFileChooser();
        if (!rutaBaseAnt.equals("")) {
            fileChooser.setCurrentDirectory(new File(rutaBaseAnt));
        }
        fileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public String getDescription() {
                return "Archivo KML (*.kml)";
            }

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                } else {
                    return f.getName().toLowerCase().endsWith(".kml");
                }
            }
        });
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int retornoSel = fileChooser.showOpenDialog(this);
        if (retornoSel == JFileChooser.APPROVE_OPTION) {
            //Se obtienen la ruta y el nombre de la capa, sin extensión
            String rutaArchivo = fileChooser.getSelectedFile().getAbsolutePath();
            int posSeparadorAux = rutaArchivo.lastIndexOf(File.separator);
            int posPuntoAux = rutaArchivo.lastIndexOf(".");

            String rutaBase;
            if (posSeparadorAux >= 0) {
                rutaBase = rutaArchivo.substring(0, posSeparadorAux + 1);
            } else {
                rutaBase = File.separator;
            }

            String descCapa;
            if (posPuntoAux > posSeparadorAux) {
                descCapa = rutaArchivo.substring(posSeparadorAux + 1, posPuntoAux);
            } else {
                descCapa = rutaArchivo.substring(posSeparadorAux + 1);
            }

            if (!rutaBase.equals(rutaBaseAnt)) {
                //Se guarda la información de la ruta de carga en el archivo de propiedades
                Utilidades.guardarRutaBase(rutaBase);
            }

            Capa capa = null;
            char tipoCapa = ' ';
            try (FileReader fr = new FileReader(rutaArchivo);
                BufferedReader br = new BufferedReader(fr)) {
                String lineaCoordenadas = "";
                boolean indEnCoordenadas = false;
                String linea;
                while ((linea = br.readLine()) != null) {
                    if (tipoCapa == ' ') {
                        //Se determina el tipo de capa
                        tipoCapa = obtenerTipoCapa(linea);
                    }

                    if (linea.toLowerCase().contains("<coordinates>")) {
                        lineaCoordenadas = "";
                        indEnCoordenadas = true;
                    }

                    if (indEnCoordenadas) {
                        lineaCoordenadas += linea;
                    }

                    if (linea.toLowerCase().contains("</coordinates>")) {
                        indEnCoordenadas = false;
                    }

                    if (tipoCapa != ' ' && !lineaCoordenadas.equals("") && !indEnCoordenadas) {
                        //Se almacenan los datos en memoria
                        capa = cargarObjetoCapa(capa, tipoCapa, lineaCoordenadas, rutaArchivo, descCapa);
                        lineaCoordenadas = "";
                    }
                }
            } catch (IOException ex) {
            }

            //Se agrega el archivo al mapa de capas y se recarga la tabla
            if (tipoCapa != ' ') {
                this.mapaCapasKML.put(rutaArchivo, capa);
                this.cargarTablaKML();
                this.actualizarComboCapas();
            }
            
            this.mostrarResultados();
        }
    }//GEN-LAST:event_btnBuscarArchivoActionPerformed

    private void btnCalcularActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCalcularActionPerformed
        //Se valida que se hayan diligenciado todos los campos
        if (this.validarCamposCalculo()) {
            //Se inhabilitan los componentes
            this.habilitarComponentes(false);

            //Se inicia el hilo que muestra el avance del proceso
            final AvanceCalculo tareaCalculo = new AvanceCalculo();
            Thread hiloProcesar = new Thread(tareaCalculo, "Procesando");
            hiloProcesar.start();

            Runnable procesoCarga = new Runnable() {
                @Override
                public void run() {
                    datosCapaEventos = (EntradaGenerica) cmbCapaEventos.getSelectedItem();
                    anchoBanda = Double.parseDouble(txtAnchoBanda.getText());
                    resolucion = Double.parseDouble(txtResolucion.getText());
                    funcionNucleo = (FuncionNucleo) cmbFuncionNucleo.getSelectedItem();
                    try {
                        lblAvance.setVisible(true);

                        //Se llama a la clase que realiza el cálculo
                        PrCalculoKDE prCalculoKDE = new PrCalculoKDE(datosCapaEventos.getLlave(), anchoBanda, resolucion, funcionNucleo, mapaCapasKML);

                        //Se realiza el cálculo
                        arrPixels = prCalculoKDE.calcularKDE();
                    } finally {
                        tareaCalculo.setControlCorrer(false);
                        lblAvance.setVisible(false);
                        habilitarComponentes(true);
                    }

                    if (arrPixels != null && arrPixels.length > 0) {
                        habilitarComponentes(true);
                        
                        mostrarResultados();
                    } else {
                        JOptionPane.showMessageDialog(frmPrincipal, "Error en el calculo del método KCE", "KDE", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };

            Thread hiloCarga = new Thread(procesoCarga, "procesarCarga");
            hiloCarga.start();
        }
    }//GEN-LAST:event_btnCalcularActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscarArchivo;
    private javax.swing.JButton btnCalcular;
    private javax.swing.JButton btnExportarKDE;
    private javax.swing.JCheckBox chkLeyenda;
    private javax.swing.JComboBox cmbCapaEventos;
    private javax.swing.JComboBox cmbFuncionNucleo;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel lblAvance;
    private javax.swing.JPanel panContenedor;
    private javax.swing.JPanel panResultados;
    private javax.swing.JTable tblKML;
    private javax.swing.JTextField txtAnchoBanda;
    private javax.swing.JTextField txtResolucion;
    // End of variables declaration//GEN-END:variables

    private class FrmMostrarKDEListener implements InternalFrameListener {

        @Override
        public void internalFrameOpened(InternalFrameEvent e) {

        }

        @Override
        public void internalFrameClosing(InternalFrameEvent e) {

        }

        @Override
        public void internalFrameClosed(InternalFrameEvent e) {
            frmPrincipal.remove(frmMostrarKDE);
        }

        @Override
        public void internalFrameIconified(InternalFrameEvent e) {

        }

        @Override
        public void internalFrameDeiconified(InternalFrameEvent e) {

        }

        @Override
        public void internalFrameActivated(InternalFrameEvent e) {

        }

        @Override
        public void internalFrameDeactivated(InternalFrameEvent e) {

        }

    }
    
    private class ButtonRendererKDE extends JButton implements TableCellRenderer {

        public void setOpaque() {
            super.setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(UIManager.getColor("Button.background"));
            }
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    private class ButtonEditorKDE extends DefaultCellEditor {

        protected JButton button;
        private String label;
        private boolean isPushed;
        private final JTable table;
        private final String[] arrRutas;

        public ButtonEditorKDE(JCheckBox checkBox, JTable table, String[] arrRutas) {
            super(checkBox);
            this.table = table;
            this.arrRutas = arrRutas;

            this.button = new JButton();
            this.button.setOpaque(true);
            this.button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            if (isSelected) {
                this.button.setForeground(table.getSelectionForeground());
                this.button.setBackground(table.getSelectionBackground());
            } else {
                this.button.setForeground(table.getForeground());
                this.button.setBackground(table.getBackground());
            }
            this.label = (value == null) ? "" : value.toString();
            this.button.setText(this.label);
            this.isPushed = true;
            return this.button;
        }

        @Override
        public Object getCellEditorValue() {
            if (this.isPushed) {
                int seleccionAux = JOptionPane.showConfirmDialog(frmPrincipal, "¿Desea quitar la capa seleccionada?", "Quitar capa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (seleccionAux == JOptionPane.YES_OPTION) {
                    habilitarComponentes(false);
                    String rutaAux = this.arrRutas[this.table.getSelectedRow()];
                    mapaCapasKML.remove(rutaAux);
                    cargarTablaKML();
                    actualizarComboCapas();
                    habilitarComponentes(true);
                    
                    mostrarResultados();
                }
            }
            this.isPushed = false;
            return this.label;
        }

        @Override
        public boolean stopCellEditing() {
            this.isPushed = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }
    
}
