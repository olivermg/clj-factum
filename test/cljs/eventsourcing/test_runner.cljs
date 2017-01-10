(ns eventsourcing.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [eventsourcing.core-test]
   [eventsourcing.common-test]))

(enable-console-print!)

(doo-tests 'eventsourcing.core-test
           'eventsourcing.common-test)
