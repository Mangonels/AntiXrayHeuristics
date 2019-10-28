package es.mithrandircraft.antixrayheuristics.callbacks;

import java.util.List;

public interface GetAllBaseXrayerDataCallback {
    void onQueryDone(List<String> uuids, List<Integer> handledamounts, List<String> firsthandledtimes);
}
