package software.amazon.redshift.clusterparametergroup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import software.amazon.awssdk.services.redshift.model.CreateTagsRequest;
import software.amazon.awssdk.services.redshift.model.DeleteTagsRequest;

@AllArgsConstructor
@Builder
@Data
public class ModifyTagsRequest {
    private CreateTagsRequest createNewTagsRequest;
    private DeleteTagsRequest deleteOldTagsRequest;
}
