package helpingTools.lsmTree.treeUtils;

import helpingTools.lsmTree.model.LSMTree;

import java.util.List;
import java.util.TimerTask;

import static helpingTools.lsmTree.treeUtils.SsTable.segmentMaxSize;

public class CompactTask extends TimerTask {
    private final List<LSMTree> trees;

    public CompactTask(List<LSMTree> trees) {
        this.trees = trees;
    }

    @Override
    public void run() {
        try {
            for(LSMTree tree : trees) {
                int SsTablesCount = tree.getSsTables().size();
                if(SsTablesCount > 5) {
                    for(int i=0; i<SsTablesCount; i+=2) {
                        // check if the tree is the last tree in odd group
                        if(!(SsTablesCount%2==1 && i==SsTablesCount-1)) {
                            long firstSegmentLength = tree.getSsTables().get(i).getSegmentFile().length();
                            long secondSegmentLength = tree.getSsTables().get(i+1).getSegmentFile().length();
                            // check if segment exceeds max length
                            if(firstSegmentLength<segmentMaxSize && secondSegmentLength<segmentMaxSize) {
                                tree.compact(tree.getSsTables().get(i+1), tree.getSsTables().get(i));
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }
}
