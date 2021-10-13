package test.model;

import com.rick.db.config.annotation.ColumnName;
import com.rick.db.config.annotation.TableName;
import com.rick.db.dto.BasePureEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * @author Rick
 * @createdAt 2021-09-27 09:17:00
 */
@TableName("t_project")
@SuperBuilder
@Getter
@Setter
public class ProjectGroup extends BasePureEntity {

    private String title;

    private String startTime;

    @ColumnName("end_time2")
    private String endTime;
}
