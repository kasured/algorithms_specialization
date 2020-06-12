/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package space.kasured.coursera;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Arrays;

@State(Scope.Benchmark)
public class Sorting {

    int[] input = new int[]{7,1,3,2,4,5,6};

    @Benchmark
    public void countSort(Blackhole consumer) {
        consumer.consume(countSort(input));
    }

    private static int[] countSort(int[] arr) {
        final int len = arr.length;
        final int[] counter = new int[len + 1];

        for(int val: arr) {
            counter[val]++;
        }

        int runningCount = 0;
        for(int i = 0; i <= len; i++) {
            runningCount = runningCount + counter[i];
            counter[i] = runningCount;
        }

        // place into proper position
        int swaps = 0;

        int i = 0;

        while(i < len) {
            int properPlace = counter[arr[i]] - 1;
            //counter[arr[i]] = counter[arr[i]] - 1;
            if(i != properPlace) {
                swap(arr, i, properPlace);
                swaps++;
            } else {
                i++;
            }
        }

        System.out.println(swaps);
        System.out.println(Arrays.toString(arr));

        return arr;
    }

    private static void swap(int[] a, int i, int j) {
        if(a[i] != a[j]) {
            a[i] = a[i] ^ a[j];
            a[j] = a[i] ^ a[j];
            a[i] = a[i] ^ a[j];
        }
    }

}
