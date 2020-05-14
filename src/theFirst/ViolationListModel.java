package theFirst;

import java.util.Vector;

import javax.swing.AbstractListModel;

public class ViolationListModel extends AbstractListModel {

    /**
     *
     */
    private static final long serialVersionUID = 3192268898876155725L;
    protected Vector list;

    public ViolationListModel(Vector list) {
        this.list = list;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.ListModel#getElementAt(int)
     */
    @Override
    public Object getElementAt(int index) {
        try {
            return list.get(index);
        } catch (IndexOutOfBoundsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.ListModel#getSize()
     */
    @Override
    public int getSize() {
        if(list==null){
            return 0;
        }
        return list.size();
    }

}