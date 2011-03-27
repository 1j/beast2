package beast.app.beauti;


import java.awt.BorderLayout;
import java.awt.Component;
import java.net.URL;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;

import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import beast.app.draw.InputEditor;
import beast.app.draw.PluginPanel;
import beast.app.draw.InputEditor.EXPAND;
import beast.core.Input;
import beast.core.Plugin;
import beast.evolution.alignment.Alignment;

/** panel making up each of the tabs in Beauti **/
public class BeautiPanel extends JPanel implements ListSelectionListener {
	private static final long serialVersionUID = 1L;
	/** box containing the list, to make (in)visible on update **/
	Box m_listBox;
	JList m_listOfPartitions;
    DefaultListModel m_listModel;
    BeautiDoc m_doc;
    
    int m_iPanel;
    int m_iPartition;

	public BeautiPanel(int iPanel, BeautiDoc doc, boolean bHasPartion) throws Exception {
		m_doc = doc;
		m_iPanel = iPanel;
	    setLayout(new BorderLayout());
	    refreshPanel();
	    addParitionPanel(bHasPartion, iPanel);
	} // c'tor
	
    void addParitionPanel(boolean bHasPartion, int iPanel) {
		Box box = Box.createVerticalBox();
    	if (bHasPartion) {
    		m_listBox = createList(); 
			box.add(m_listBox);
    	}
		box.add(Box.createVerticalGlue());
		box.add(new JLabel(getIcon(iPanel)));
    	add(box, BorderLayout.WEST);
	}
	
    Box createList() {
		Box partitionBox = Box.createVerticalBox();
		partitionBox.setAlignmentX(LEFT_ALIGNMENT);
		partitionBox.add(new JLabel("partition"));
        m_listModel = new DefaultListModel();
    	m_listOfPartitions = new JList(m_listModel);
    	m_listOfPartitions.addListSelectionListener(this);
    	for (Alignment data : m_doc.m_alignments.get()) {
    		m_listModel.addElement(data);
    	}
    	m_listOfPartitions.setBorder(new BevelBorder(BevelBorder.RAISED));
    	partitionBox.add(m_listOfPartitions);
    	partitionBox.setBorder(new EtchedBorder());
    	return partitionBox;
    }

	static ImageIcon getIcon(int iPanel) {
//		String sURL = ModelBuilder.ICONPATH + "../../beauti/" + iPanel + ".png"; 
//	    ImageIcon icon = new ImageIcon((URL)ClassLoader.getSystemResource(sURL));
//	    BufferedImage img = new BufferedImage(100,100,BufferedImage.TYPE_INT_ARGB);
//	    Graphics2D g = img.createGraphics();
//	    g.drawImage(icon.getImage(), 0, 0, 100, 100, 0, 0, icon.getIconWidth(), icon.getIconHeight(), null);
//	    icon = new ImageIcon(img);
//	    return icon;

        String sIconLocation = BeautiInitDlg.ICONPATH + iPanel +".png";
        try {
	        URL url = (URL)ClassLoader.getSystemResource(sIconLocation);
	        if (url == null) {
	        	System.err.println("Cannot find icon " + sIconLocation);
	        	return null;
	        }
	        ImageIcon icon = new ImageIcon(url);
	        return icon;
        } catch (Exception e) {
        	System.err.println("Cannot load icon " + sIconLocation + " " + e.getMessage());
        	return null;
		}
	
	}
	
	
	void refreshPanel() throws Exception {
		switch (m_iPanel) {
			case Beauti.DATA_PANEL : refreshInputPanel(m_doc, m_doc.m_likelihoods, true, EXPAND.FALSE);break;
			case Beauti.TAXON_SETS_PANEL : refreshInputPanel(m_doc, m_doc.m_taxonset, false, EXPAND.FALSE);break;
			case Beauti.TIP_DATES_PANEL : refreshInputPanel(m_doc, m_doc.m_tipdates, false, EXPAND.TRUE);break;
			case Beauti.SITE_MODEL_PANEL : refreshInputPanel(m_doc, m_doc.m_siteModel, false, EXPAND.TRUE);break;
			case Beauti.CLOCK_MODEL_PANEL : refreshInputPanel(m_doc, m_doc.m_clockModel, false, EXPAND.TRUE);break;
			case Beauti.TREE_PRIOR_PANEL : refreshInputPanel(m_doc, m_doc.m_treeprior, false, EXPAND.TRUE);break;
			case Beauti.STATE_PANEL : refreshInputPanel(m_doc.m_mcmc.get().m_startState.get(), m_doc.m_mcmc.get().m_startState.get().stateNodeInput, true, EXPAND.TRUE);break;
			case Beauti.PRIORS_PANEL : refreshInputPanel(m_doc, m_doc.m_priors, true, EXPAND.IF_ONE_ITEM);break;
			case Beauti.OPERATORS_PANEL : refreshInputPanel(m_doc.m_mcmc.get(), m_doc.m_mcmc.get().operatorsInput, true, EXPAND.FALSE);break;
			case Beauti.MCMC_PANEL : refreshInputPanel(m_doc, m_doc.m_mcmc, false, EXPAND.TRUE);break;
		}
		if (m_listBox != null) {
			m_listBox.setVisible(m_doc.m_alignments.get().size() > 1);
		}
	}
	
	Component m_centralComponent = null;
	void refreshInputPanel(Plugin plugin, Input<?> input, boolean bAddButtons, EXPAND bForceExpansion) throws Exception {
		if (m_centralComponent != null) {
			remove(m_centralComponent);
		}
	    if (input != null && input.get() != null) {
	        InputEditor inputEditor = PluginPanel.createInputEditor(input, plugin, bAddButtons, bForceExpansion, null);
	        Box box = Box.createVerticalBox();
	        box.add(inputEditor);
	        box.add(Box.createGlue());
	        JScrollPane scroller = new JScrollPane(box);
	        m_centralComponent = scroller;
	    } else {
	        m_centralComponent = new JLabel("Nothing to be specified");
	    }
        add(m_centralComponent, BorderLayout.CENTER);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		m_doc.sync(m_iPanel);
		m_iPartition = m_listOfPartitions.getSelectedIndex();
		m_doc.syncTo(m_iPanel, m_iPartition);
		try {
			refreshPanel();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

} // class BeautiPanel
