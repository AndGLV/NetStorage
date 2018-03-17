package files;

import constants.NetConstants;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NetTreeFiles implements Serializable{
	private NetNode root;

	public NetNode returnNewNode(){
	    return new NetNode();
    }



	public class NetNode implements Serializable{
		private NetNode parent;
		private CopyOnWriteArrayList<NetNode> childrens;
		private NetFile value;

		public void addChildren(NetNode children){
			childrens.add(children);
		}
		public void addChildren(NetFile children) {
		    NetNode nn = new NetNode();
		    nn.setParent(this);
		    nn.setValue(children);
		    nn.setChildrens(new CopyOnWriteArrayList<>());
		    childrens.add(nn);
        }

		public void setParent(NetNode parent) {
			this.parent = parent;
		}

		public void setChildrens(CopyOnWriteArrayList<NetNode> childrens) {
			this.childrens = childrens;
		}

		public void setValue(NetFile value) {
			this.value = value;
		}

		public NetNode getParent() {
			return parent;
		}

		public CopyOnWriteArrayList<NetNode> getChildrens() {
			return childrens;
		}

		public NetFile getValue() {
			return value;
		}

		public void deleteChildren(NetNode chld){
		    childrens.remove(chld);
        }

        public void deleteChildren(NetFile chld){
            for (NetNode children : childrens) {
                if (children.getValue() == chld){
                    childrens.remove(children);
                }
            }
        }
	}

	public NetNode getNodeFromValue(NetFile value){
        NetNode[] res = new NetNode[1];
        getNodeRec(root, value, res);
        return res[0];
	}

	public NetNode getNodeFromPath(String path){
        NetNode[] res = new NetNode[1];
        getNodeRecPath(root, path, res);
        return res[0];
    }

    private void getNodeRecPath(NetNode start, String value, NetNode[] res){
        if (start.getValue().getPath() == value){
            res[0] = start;
        } else{
            List<NetNode> childs = start.getChildrens();
            if (!childs.isEmpty()){
                for (NetNode child : childs) {
                    getNodeRecPath(child, value, res);
                }
            }
        }
    }

	private void getNodeRec(NetNode start, NetFile value, NetNode[] res){
	    if (start.getValue() == value){
	        res[0] = start;
        } else{
            List<NetNode> childs = start.getChildrens();
            if (!childs.isEmpty()){
                for (NetNode child : childs) {
                    getNodeRec(child, value, res);
                }
            }
        }
    }

	public NetTreeFiles(String pathUser) {
		root = new NetNode();
		NetFile rootNetFile = new NetFile();
		rootNetFile.setName("root");
		rootNetFile.setPath(pathUser);
		rootNetFile.setDirectory(true);

		root.setParent(null);
		root.setChildrens(new CopyOnWriteArrayList<>());
		root.setValue(rootNetFile);
	}

	public NetNode getRoot() {
		return root;
	}

	public void addNode(NetNode parent, NetFile value){
		NetNode newNode = new NetNode();
		newNode.setParent(parent);
		newNode.setValue(value);
		newNode.setChildrens(new CopyOnWriteArrayList<>());
		parent.addChildren(newNode);
	}
}
