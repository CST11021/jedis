package redis.clients.jedis;

import static redis.clients.jedis.Protocol.Keyword.ALPHA;
import static redis.clients.jedis.Protocol.Keyword.ASC;
import static redis.clients.jedis.Protocol.Keyword.BY;
import static redis.clients.jedis.Protocol.Keyword.DESC;
import static redis.clients.jedis.Protocol.Keyword.GET;
import static redis.clients.jedis.Protocol.Keyword.LIMIT;
import static redis.clients.jedis.Protocol.Keyword.NOSORT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import redis.clients.jedis.util.SafeEncoder;

/**
 * Builder Class for {@link Jedis#sort(String, SortingParams) SORT} Parameters.
 */
public class SortingParams {

    private List<byte[]> params = new ArrayList<byte[]>();

    /**
     * # 先将要使用的数据加入到数据库中
     *
     * # admin
     *
     * redis> LPUSH user_id 1
     * (integer) 1
     * redis> SET user_name_1 admin
     * OK
     * redis> SET user_level_1 9999
     * OK
     *
     * # huangz
     *
     * redis> LPUSH user_id 2
     * (integer) 2
     * redis> SET user_name_2 huangz
     * OK
     * redis> SET user_level_2 10
     * OK
     *
     * # jack
     *
     * redis> LPUSH user_id 59230
     * (integer) 3
     * redis> SET user_name_59230 jack
     * OK
     * redis> SET user_level_59230 3
     * OK
     *
     * # hacker
     *
     * redis> LPUSH user_id 222
     * (integer) 4
     * redis> SET user_name_222 hacker
     * OK
     * redis> SET user_level_222 9999
     * OK
     * 如果希望按 level 从大到小排序 user_id ，可以使用以下命令：
     *
     * redis> SORT user_id BY user_level_* DESC
     * 1) "222"    # hacker
     * 2) "1"      # admin
     * 3) "2"      # huangz
     * 4) "59230"  # jack
     *
     * 但是有时候只是返回相应的 id 没有什么用，你可能更希望排序后返回 id 对应的用户名，这样更友好一点，使用 GET 选项可以做到这一点：
     *
     * redis> SORT user_id BY user_level_* DESC GET user_name_*
     * 1) "hacker"
     * 2) "admin"
     * 3) "huangz"
     * 4) "jack"
     *
     * @param pattern
     * @return the SortingParams Object
     */
    public SortingParams by(final String pattern) {
        return by(SafeEncoder.encode(pattern));
    }

    /**
     * Sort by weight in keys.
     * <p>
     * Takes a pattern that is used in order to generate the key names of the weights used for
     * sorting. Weight key names are obtained substituting the first occurrence of * with the actual
     * value of the elements on the list.
     * <p>
     * The pattern for a normal key/value pair is "field*" and for a value in a hash
     * "field*-&gt;fieldname".
     *
     * @param pattern
     * @return the SortingParams Object
     */
    public SortingParams by(final byte[] pattern) {
        params.add(BY.raw);
        params.add(pattern);
        return this;
    }

    /**
     * No sorting.
     * <p>
     * This is useful if you want to retrieve a external key (using {@link #get(String...) GET}) but
     * you don't want the sorting overhead.
     *
     * @return the SortingParams Object
     */
    public SortingParams nosort() {
        params.add(BY.raw);
        params.add(NOSORT.raw);
        return this;
    }

    public Collection<byte[]> getParams() {
        return Collections.unmodifiableCollection(params);
    }

    /**
     * Get the Sorting in Descending Order.
     *
     * @return the sortingParams Object
     */
    public SortingParams desc() {
        params.add(DESC.raw);
        return this;
    }

    /**
     * Get the Sorting in Ascending Order. This is the default order.
     *
     * @return the SortingParams Object
     */
    public SortingParams asc() {
        params.add(ASC.raw);
        return this;
    }

    /**
     * # 将数据一一加入到列表中
     *
     * redis> LPUSH rank 30
     * (integer) 1
     * redis> LPUSH rank 56
     * (integer) 2
     * redis> LPUSH rank 42
     * (integer) 3
     * redis> LPUSH rank 22
     * (integer) 4
     * redis> LPUSH rank 0
     * (integer) 5
     * redis> LPUSH rank 11
     * (integer) 6
     * redis> LPUSH rank 32
     * (integer) 7
     * redis> LPUSH rank 67
     * (integer) 8
     * redis> LPUSH rank 50
     * (integer) 9
     * redis> LPUSH rank 44
     * (integer) 10
     * redis> LPUSH rank 55
     * (integer) 11
     *
     * # 排序
     *
     * redis> SORT rank LIMIT 0 5  # 返回排名前五的元素
     * 1) "0"
     * 2) "11"
     * 3) "22"
     * 4) "30"
     * 5) "32"
     *
     * 修饰符可以组合使用。以下例子返回降序(从大到小)的前 5 个对象。
     *
     * redis> SORT rank LIMIT 0 5 DESC
     * 1) "78"
     * 2) "67"
     * 3) "56"
     * 4) "55"
     * 5) "50"
     *
     * @param start is zero based
     * @param count
     * @return the SortingParams Object
     */
    public SortingParams limit(final int start, final int count) {
        params.add(LIMIT.raw);
        params.add(Protocol.toByteArray(start));
        params.add(Protocol.toByteArray(count));
        return this;
    }

    /**
     * # 将数据一一加入到列表中
     *
     * redis> LPUSH website "www.reddit.com"
     * (integer) 1
     * redis> LPUSH website "www.slashdot.com"
     * (integer) 2
     * redis> LPUSH website "www.infoq.com"
     * (integer) 3
     *
     * # 默认排序
     *
     * redis> SORT website
     * 1) "www.infoq.com"
     * 2) "www.slashdot.com"
     * 3) "www.reddit.com"
     *
     * # 按字符排序
     *
     * redis> SORT website ALPHA
     * 1) "www.infoq.com"
     * 2) "www.reddit.com"
     * 3) "www.slashdot.com"
     *
     * @return the SortingParams Object
     */
    public SortingParams alpha() {
        params.add(ALPHA.raw);
        return this;
    }

    /**
     * # 先将要使用的数据加入到数据库中
     *
     * # admin
     *
     * redis> LPUSH user_id 1
     * (integer) 1
     * redis> SET user_name_1 admin
     * OK
     * redis> SET user_level_1 9999
     * OK
     *
     * # huangz
     *
     * redis> LPUSH user_id 2
     * (integer) 2
     * redis> SET user_name_2 huangz
     * OK
     * redis> SET user_level_2 10
     * OK
     *
     * # jack
     *
     * redis> LPUSH user_id 59230
     * (integer) 3
     * redis> SET user_name_59230 jack
     * OK
     * redis> SET user_level_59230 3
     * OK
     *
     * # hacker
     *
     * redis> LPUSH user_id 222
     * (integer) 4
     * redis> SET user_name_222 hacker
     * OK
     * redis> SET user_level_222 9999
     * OK
     * 如果希望按 level 从大到小排序 user_id ，可以使用以下命令：
     *
     * redis> SORT user_id BY user_level_* DESC
     * 1) "222"    # hacker
     * 2) "1"      # admin
     * 3) "2"      # huangz
     * 4) "59230"  # jack
     *
     * 但是有时候只是返回相应的 id 没有什么用，你可能更希望排序后返回 id 对应的用户名，这样更友好一点，使用 GET 选项可以做到这一点：
     *
     * redis> SORT user_id BY user_level_* DESC GET user_name_*
     * 1) "hacker"
     * 2) "admin"
     * 3) "huangz"
     * 4) "jack"
     *
     * @param patterns
     * @return the SortingParams Object
     */
    public SortingParams get(String... patterns) {
        for (final String pattern : patterns) {
            params.add(GET.raw);
            params.add(SafeEncoder.encode(pattern));
        }
        return this;
    }

    /**
     * Retrieving external keys from the result of the search.
     * <p>
     * Takes a pattern that is used in order to generate the key names of the result of sorting. The
     * key names are obtained substituting the first occurrence of * with the actual value of the
     * elements on the list.
     * <p>
     * The pattern for a normal key/value pair is "field*" and for a value in a hash
     * "field*-&gt;fieldname".
     * <p>
     * To get the list itself use the char # as pattern.
     *
     * @param patterns
     * @return the SortingParams Object
     */
    public SortingParams get(byte[]... patterns) {
        for (final byte[] pattern : patterns) {
            params.add(GET.raw);
            params.add(pattern);
        }
        return this;
    }
}
