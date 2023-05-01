package ro.mta.sdk.repository;

public interface BackupHandler<T> {
    T read();

    void write(T collection);
}
