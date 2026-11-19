package com.example.aggregator.scheduling;

import com.example.aggregator.http.HttpClientException;
import com.example.aggregator.model.WriteMode;
import com.example.aggregator.source.DataSource;
import com.example.aggregator.source.DataSourceException;
import com.example.aggregator.storage.DataStorage;
import com.example.aggregator.storage.StorageException;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

class PollingTaskTest {

    @Test
    void testRunHttpError() {
        DataSource source = mock(DataSource.class);
        when(source.fetch(anyMap())).thenThrow(new HttpClientException("net err"));
        DataStorage storage = mock(DataStorage.class);

        new PollingTask(source, Map.of(), storage, WriteMode.APPEND).run();
        verify(storage, never()).write(anyList(), any());
    }

    @Test
    void testRunDataSourceError() {
        DataSource source = mock(DataSource.class);
        when(source.fetch(anyMap())).thenThrow(new DataSourceException("parse err", null));
        DataStorage storage = mock(DataStorage.class);

        new PollingTask(source, Map.of(), storage, WriteMode.APPEND).run();
        verify(storage, never()).write(anyList(), any());
    }

    @Test
    void testRunStorageError() {
        DataSource source = mock(DataSource.class);
        when(source.fetch(anyMap())).thenReturn(List.of());
        DataStorage storage = mock(DataStorage.class);
        doThrow(new StorageException("disk", null)).when(storage).write(anyList(), any());

        new PollingTask(source, Map.of(), storage, WriteMode.APPEND).run();
    }
}