package org.covaid.ui.doctor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import org.condast.commons.strings.StringStyler;
import org.condast.commons.ui.table.AbstractTableViewerWithDelete;
import org.condast.commons.ui.widgets.IStoreWithDelete;
import org.covaid.core.data.DoctorData;
import org.covaid.core.data.DoctorData.States;
import org.covaid.core.doctor.DoctorDataEvent;
import org.covaid.core.doctor.IDoctorDataListener;
import org.covaid.ui.images.CovaidImages;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class CovaidTestTableViewer extends AbstractTableViewerWithDelete<DoctorData>{
	private static final long serialVersionUID = 1L;

	private enum Columns{
		DATE,
		ID,
		ILL;

		@Override
		public String toString() {
			return StringStyler.prettyString( super.toString() );//NaLanguage.getInstance().getString( this );
		}

		public static int getWeight( Columns column ){
			switch( column ){
			case DATE:
			case ID:
			case ILL:
				return 50;
			default:
				return 30;
			}
		}
	}

	private Collection<IDoctorDataListener> dlisteners;

	public CovaidTestTableViewer(Composite parent,int style ) {
		super(parent,style, true );
		this.dlisteners = new ArrayList<>();
	}

	public void addSelectionChangeListener( ISelectionChangedListener listener ) {
		TableViewer viewer = super.getViewer();
		viewer.addSelectionChangedListener(listener);
	}

	public void removeSelectionChangeListener( ISelectionChangedListener listener ) {
		TableViewer viewer = super.getViewer();
		viewer.remove(listener);
	}

	public void addDoctorListener(IDoctorDataListener listener) {
		this.dlisteners.add(listener);
	}

	public void removeDoctorListener(IDoctorDataListener listener) {
		this.dlisteners.remove(listener);
	}

	@Override
	protected void createContentComposite( Composite parent,int style ){
		super.createContentComposite(parent, style);
		TableViewer viewer = super.getViewer();
		for( Columns column: Columns.values() ){
			createColumn( column );
		}
		String deleteStr = Buttons.DELETE.toString();
		super.createDeleteColumn( Columns.values().length, deleteStr, 10 );	
		viewer.setLabelProvider( new DoctorDataLabelProvider() );
		viewer.addDoubleClickListener((e)->{
			
		});
	}

	public void setInput( Collection<DoctorData> data ){
		super.setInput( data );
	}
	
	public int getSelectionIndex( DoctorData data ) {
		Collection<IStoreWithDelete<DoctorData>> ddata = this.getStoreInput();
		int index = 0;
		for( IStoreWithDelete<DoctorData> dd: ddata ) {
			if( dd.getStore().equals( data))
				return index;
			index++;
		}
		return index;
	}

	@Override
	protected void onRowDoubleClick(DoctorData selection) {
	}

	@Override
	protected void onButtonCreated(Buttons type, Button button) {
		GridData gd_button = new GridData(32, 32);
		gd_button.horizontalAlignment = SWT.RIGHT;
		button.setLayoutData(gd_button);
		button.setText("");
	}

	@Override
	protected boolean onAddButtonSelected(SelectionEvent e) {
		boolean result = false;
		try{
		}
		catch( Exception ex ){
			ex.printStackTrace();
		}
		return result;
	}
	
	@Override
	protected boolean onDeleteButton( Collection<DoctorData> deleted ) {
		for( IDoctorDataListener listener: this.dlisteners )
			for( DoctorData data: deleted )
				listener.notifyDoctorDoctorChanged( new DoctorDataEvent(this, IDoctorDataListener.DocterDataEvents.REMOVE, data));
		return true;
	}

	private TableViewerColumn createColumn( final Columns column ) {
		TableViewerColumn result = super.createColumn( column.toString(), column.ordinal(), Columns.getWeight(column) );
		if( Columns.ILL.equals(column))
			result.setEditingSupport(new StatesEditingSupport( super.getViewer()));
		return result;
	}
	
	@Override
	protected void onRefresh() {
		//super.setInput( Arrays.asList( manager.getVessels() ));
	}
	
	private class DoctorDataLabelProvider extends DeleteLabelProvider implements ITableLabelProvider{
		private static final long serialVersionUID = 1L;

		@SuppressWarnings("unchecked")
		@Override
		public String getColumnText( Object element, int columnIndex ) {
			String retval = super.getColumnText(element, columnIndex);
			if( retval != null )
				return retval;
			Columns column = Columns.values()[ columnIndex ];
			IStoreWithDelete<DoctorData> swd = (IStoreWithDelete<DoctorData>) element;
			DoctorData data = swd.getStore();
			switch( column){
			case DATE:
				SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
				retval = formatter.format(data.getDate());
				break;		
			case ID:
				retval = String.valueOf( data.getId());
				break;
			case ILL:
				retval = data.getState().toString();
				break;
			default:
				break;				
			}
			swd.addText(retval);
			return retval;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Image getColumnImage(Object arg0, int columnIndex) {
			Image image = super.getColumnImage(arg0, columnIndex);
			if( columnIndex == getDeleteColumnindex() ){
				return image;
			}
			Columns column = Columns.values()[ columnIndex ];
			IStoreWithDelete<DoctorData> swd = (IStoreWithDelete<DoctorData>) arg0;
			DoctorData data = swd.getStore();
			CovaidImages images = CovaidImages.getInstance();
			switch( column){
			case ILL:
				image = images.getImage( CovaidImages.Images.APPOINTMENT);
				switch( data.getState()) {
				case POSITIVE:
					image = images.getImage( CovaidImages.Images.ILL);					
					break;
				case NEGATIVE:
					image = images.getImage( CovaidImages.Images.HEALTHY);					
					break;
				default:
					break;
				}
				break;
			default:
				break;				
			}
			return image;
		}
	}
	
	private static class StatesEditingSupport extends EditingSupport {
		private static final long serialVersionUID = 1L;
		private final TableViewer viewer;
	    private final CellEditor editor;

	    public StatesEditingSupport(TableViewer viewer) {
	        super(viewer);
	        this.viewer = viewer;
	        this.editor = new ComboBoxCellEditor(viewer.getTable(), DoctorData.States.getItems(), SWT.DROP_DOWN | SWT.READ_ONLY);
	        this.editor.setValue(0);
	    }

	    @Override
	    protected CellEditor getCellEditor(Object element) {
	        return editor;
	    }

	    @Override
	    protected boolean canEdit(Object element) {
	        return true;
	    }

	    @SuppressWarnings("unchecked")
		@Override
	    protected Object getValue(Object element) {
	        StoreWithDelete store = (StoreWithDelete) element;
	    	DoctorData data = store.getStore();
	        return data.getState().ordinal();
	    }

	    @Override
	    protected void setValue(Object element, Object value) {
	        @SuppressWarnings("unchecked")
			StoreWithDelete store = (StoreWithDelete) element;
	    	DoctorData data = store.getStore();
	        data.setState(States.getState((int) value));
	        viewer.update(element, null);
	    }
	}
}