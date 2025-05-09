/*
 *  Copyright (c) 2022-2025, Mybatis-Flex (fuhai999@gmail.com).
 *  <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.mybatisflex.coretest;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.mybatisflex.core.query.CPI;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryWrapper;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static com.mybatisflex.coretest.table.AccountTableDef.ACCOUNT;
import static com.mybatisflex.core.query.QueryMethods.*;

/**
 * Lambda 构建 SQL 测试。
 *
 * @author 王帅
 * @since 2023-10-01
 */
public class QueryWrapperTest {

    public static void printSQL(QueryWrapper queryWrapper) {
        System.out.println(SqlFormatter.format(queryWrapper.toSQL()));
    }


    /**
     * https://github.com/mybatis-flex/mybatis-flex/issues/288
     */
    @Test
    public void testIssues288() {
        final QueryWrapper query = QueryWrapper.create();
        final QueryColumn storeColumn = column("temp", "store_id");
        query.select(
                count(distinct(case_()
                    .when(string("purchasePerson").gt(1)).then(1)
                    .else_(0).end())).as("repurchasePerson")
            ).from(
                select(ACCOUNT.ID,
                    ACCOUNT.ID,
                    sum(ACCOUNT.ID).as("purchasePerson")
                ).from(ACCOUNT)
                    .where(ACCOUNT.ID.in(5))
                    .groupBy(ACCOUNT.ID,
                        ACCOUNT.ID)).as("temp")
            .groupBy(storeColumn);

        Object[] valueArray = CPI.getValueArray(query);
        Assert.assertEquals(valueArray.length, 2);
        printSQL(query);
    }


    @Test
    public void testOrderByValue(){
        QueryWrapper wrapper = QueryWrapper.create()
            .select("*")
            .from(Account.class)
            .orderBy(case_()
                .when(new QueryColumn("id").in(1, 2, 3))
                .then(1).end().asc()
            );
        Assert.assertEquals(CPI.getValueArray(wrapper).length, 3);
    }

    /**
     * https://github.com/mybatis-flex/mybatis-flex/issues/491
     */
    @Test
    public void testIssues491() {
        String demo1 = "SELECT c1 FROM ((SELECT 1 as c1) UNION ALL (SELECT 2 as c1) UNION ALL (SELECT 3 as c1)) AS `t`";

        QueryWrapper query1 = QueryWrapper.create()
            .select("c1")
            .from(QueryWrapper.create().select("1 as c1")
                .unionAll(QueryWrapper.create().select("2 as c1"))
                .unionAll(QueryWrapper.create().select("3 as c1")))
            .as("t");

       Assert.assertTrue(query1.toSQL().equals(demo1));


        String demo2 = "SELECT c1 FROM ((SELECT 1 as c1 FROM dual) UNION ALL (SELECT 2 as c1 FROM dual) UNION ALL " +
            "(SELECT 3 as c1 FROM dual)) AS `t`";

        QueryWrapper query2 = QueryWrapper.create()
            .select("c1")
            .from(QueryWrapper.create().select("1 as c1").from("dual")
                .unionAll(QueryWrapper.create().select("2 as c1").from("dual"))
                .unionAll(QueryWrapper.create().select("3 as c1").from("dual")))
            .as("t");

        Assert.assertTrue(query2.toSQL().equals(demo2));
    }

}
