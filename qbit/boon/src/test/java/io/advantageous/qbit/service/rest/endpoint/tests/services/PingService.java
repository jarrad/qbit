package io.advantageous.qbit.service.rest.endpoint.tests.services;

import io.advantageous.qbit.annotation.RequestMapping;

@RequestMapping("/")
public class PingService {


    @RequestMapping("/ping")
    public boolean ping() {
        return true;
    }

    //TODO fix root dir issue that Chris pointed out.

}
