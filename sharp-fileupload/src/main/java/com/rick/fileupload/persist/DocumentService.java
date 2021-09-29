package com.rick.fileupload.persist;

import com.rick.common.util.DateConvertUtils;
import com.rick.common.util.IdGenerator;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Rick
 * @createdAt 2021-09-29 18:15:00
 */
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentDAO documentDAO;

    public void save(Document document) {
        documentDAO.insert(parseParams(document));
    }

    public int rename(Long id, String name) {
        return documentDAO.update("name", new Object[] {name}, id);
    }

    public void saveAll(Collection<Document> documentList) {
        documentDAO.insert(documentList.stream().map(document -> parseParams(document)).collect(Collectors.toList()));
    }

    public Optional<Document> findById(Long id) {
        return documentDAO.selectById(id);
    }

    public int deleteById(Long id) {
        return documentDAO.deleteById(id);
    }

    public int deleteByIds(Collection<Long> ids) {
        return documentDAO.deleteByIds(ids);
    }

    private Object[] parseParams(Document document) {
        // id,created_at,name,extension,content_type,size,group_name,path
        long sequenceId = IdGenerator.getSequenceId();
        document.setId(sequenceId);
        document.setCreatedAt(Instant.now());
        return new Object[] {
                document.getId()
                , DateConvertUtils.unixTimeToLocalDateTime(document.getCreatedAt().toEpochMilli()), document.getName(), document.getExtension(), document.getContentType(), document.getSize(),document.getGroupName(),
                document.getPath(),
        };
    }
}